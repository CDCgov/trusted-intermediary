import logging
import threading
import time
import urllib.parse
import urllib.request
import uuid
import os

from locust import FastHttpUser, between, events, task
from locust.runners import MasterRunner

HEALTH_ENDPOINT = "/health"
AUTH_ENDPOINT = "/v1/auth/token"
ORDERS_ENDPOINT = "/v1/etor/orders"
RESULTS_ENDPOINT = "/v1/etor/results"
METADATA_ENDPOINT = "/v1/etor/metadata"
CONSOLIDATED_ENDPOINT = "/v1/etor/metadata/summary"

order_request_body = None
result_request_body = None
auth_request_body = None

in_azure = os.getenv('TEST_RUN_NAME') is not None


class SampleUser(FastHttpUser):
    # Each task gets called randomly, but the number next to '@task' denotes
    # how many more times that task will get called than other tasks.
    # Tasks with the same number get called approx. the same number of times.

    token_refresh_interval = 280
    access_token = None
    wait_time = between(1, 5)

    def on_start(self):
        self.authenticate()

        self.submission_id = None
        self.placer_order_id = None
        self.message_api_called = False
        self.sender = "flexion.simulated-hospital"

        # Start the token refreshing thread
        threading.Thread(
            target=self.authenticate_periodically, args=(), daemon=True
        ).start()

    def authenticate_periodically(self):
        while True:
            time.sleep(self.token_refresh_interval)  # Refresh token every few seconds
            self.authenticate()

    def authenticate(self):
        logging.debug("Authenticating...")
        response = self.client.post(AUTH_ENDPOINT, data=auth_request_body)
        if response.status_code == 200:
            data = response.json()
            self.access_token = data["access_token"]
        else:
            logging.error(f"Authentication failed: {response.error}")

    @task(1)
    def get_health(self):
        self.client.get(HEALTH_ENDPOINT)

    def post_message_request(self, endpoint, message):
        self.submission_id = str(uuid.uuid4())
        poi = self.placer_order_id or str(uuid.uuid4())
        self.placer_order_id = None if self.placer_order_id else poi
        response = self.client.post(
            endpoint,
            headers={
                "Authorization": self.access_token,
                "RecordId": self.submission_id,
                "Load-Test": "true",
            },
            data=message.replace("{{placer_order_id}}", poi),
        )
        if response.status_code == 200:
            self.message_api_called = True

    @task(5)
    def post_v1_etor_orders(self):
        self.post_message_request(ORDERS_ENDPOINT, order_request_body)

    @task(5)
    def post_v1_etor_results(self):
        self.post_message_request(RESULTS_ENDPOINT, result_request_body)

    @task(1)
    def get_v1_etor_metadata(self):
        if self.message_api_called:
            self.client.get(
                f"{METADATA_ENDPOINT}/{self.submission_id}",
                headers={
                    "Authorization": self.access_token,
                    "Load-Test": "true",
                },
                name=f"{METADATA_ENDPOINT}/{{id}}",
            )

    @task(1)
    def get_v1_metadata_consolidated(self):
        if self.message_api_called:
            self.client.get(
                f"{CONSOLIDATED_ENDPOINT}/{self.sender}",
                headers={"Authorization": self.access_token},
            )


@events.test_start.add_listener
def test_start(environment):
    global auth_request_body
    global order_request_body
    global result_request_body

    if isinstance(environment.runner, MasterRunner):
        # in a distributed run, the master does not typically need any test data
        return

    auth_request_body = get_auth_request_body()
    order_request_body = get_order_fhir_message()
    result_request_body = get_result_fhir_message()


@events.quitting.add_listener
def assert_stats(environment):
    if in_azure:
        # don't evaluate this in Azure because we want the locust process to succeed and Azure does its own test criteria checking
        return

    if environment.stats.total.fail_ratio > 0.01:
        logging.error("Test failed due to failure ratio > 1%")
        environment.process_exit_code = 1
    elif environment.stats.total.get_response_time_percentile(0.95) > 1000:
        logging.error("Test failed due to 95th percentile response time > 1000 ms")
        environment.process_exit_code = 1
    else:
        logging.info("Test passed!")


def get_auth_request_body():
    # set up the sample request body for the auth endpoint
    # using a valid test token found in the mock_credentials directory

    # TODO - notes/clarification on 2 different creds, plus expiration date of jwt
    # TODO - do we want to TF the tests? If yes which envs? In CDC envs, may need to adjust IP allow list on app. Also set as private endpoints in test config?
    # TODO - currently in Azure we're specifying a version for the key vault item (so if it gets updated, we'll be referencing an old version) - do we want to change this?
    if in_azure:
        auth_token = os.getenv("trusted-intermediary-valid-token-jwt")
    else:
        with open("mock_credentials/trusted-intermediary-valid-token.jwt") as f:
            auth_token = f.read()

    params = urllib.parse.urlencode(
        {"scope": "trusted-intermediary", "client_assertion": auth_token.strip()}
    )

    return params.encode("utf-8")


def get_order_fhir_message():
    # read the sample request body for the orders endpoint
    file_path = "002_ORM_O01_short.fhir"
    if not in_azure:
        file_path = "examples/Test/e2e/orders/" + file_path

    with open(file_path, "r") as f:
        return f.read()


def get_result_fhir_message():
    # read the sample request body for the results endpoint
    file_path = "001_ORU_R01_short.fhir"
    if not in_azure:
        file_path = "examples/Test/e2e/results/" + file_path

    with open(file_path, "r") as f:
        return f.read()
