import time
import logging
import threading
import urllib.parse
import urllib.request

from locust import FastHttpUser, task, events
from locust.runners import MasterRunner

HEALTH_ENDPOINT = "/health"
AUTH_ENDPOINT = "/v1/auth/token"
DEMOGRAPHICS_ENDPOINT = "/v1/etor/demographics"
ORDERS_ENDPOINT = "/v1/etor/orders"
METADATA_ENDPOINT = "/v1/etor/metadata"

demographics_request_body = None
order_request_body = None
auth_request_body = None


class SampleUser(FastHttpUser):
    # Each task gets called randomly, but the number next to '@task' denotes
    # how many more times that task will get called than other tasks.
    # Tasks with the same number get called approx. the same number of times.

    token_refresh_interval = 280
    access_token = None
    submission_id = "1a2b3c"

    def on_start(self):
        self.authenticate()

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
        data = response.json()
        self.access_token = data["access_token"]

    @task
    def get_health(self):
        self.client.get(HEALTH_ENDPOINT)

    @task(5)
    def post_v1_etor_demographics(self):
        self.client.post(
            DEMOGRAPHICS_ENDPOINT,
            data=demographics_request_body,
            headers={"Authorization": self.access_token},
        )

    @task(5)
    def post_v1_etor_orders(self):
        headers={"Authorization": self.access_token, "RecordId": self.submission_id},
        self.client.post(
            ORDERS_ENDPOINT,
            headers,
            data=order_request_body,
        )

    @task(5)
    def get_v1_etor_metadata(self):
        self.client.get(
        f"{METADATA_ENDPOINT}/{self.submission_id}",
        headers={"Authorization": self.access_token},
        )


@events.test_start.add_listener
def test_start(environment):
    global demographics_request_body
    global auth_request_body
    global order_request_body

    if isinstance(environment.runner, MasterRunner):
        # in a distributed run, the master does not typically need any test data
        return

    demographics_request_body = get_demographics_request_body()
    auth_request_body = get_auth_request_body()
    order_request_body = get_orders_request_body()


@events.quitting.add_listener
def assert_stats(environment):
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
    auth_scope = "report-stream"
    with open("mock_credentials/report-stream-valid-token.jwt") as f:
        auth_token = f.read()
    params = urllib.parse.urlencode(
        {"scope": auth_scope, "client_assertion": auth_token.strip()}
    )
    return params.encode("utf-8")


def get_demographics_request_body():
    # read the sample request body for the demographics endpoint
    with open("examples/fhir/newborn_patient.json", "r") as f:
        return f.read()


def get_orders_request_body():
    # read the sample request body for the orders endpoint
    with open("examples/fhir/lab_order.json", "r") as f:
        return f.read()
