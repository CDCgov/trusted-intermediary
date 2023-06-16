import json
import urllib.parse
import urllib.request

from locust import HttpUser, task, events
from locust.runners import MasterRunner


HEALTH_ENDPOINT = "/health"
AUTH_ENDPOINT = "/v1/auth"
DEMOGRAPHICS_ENDPOINT = "/v1/etor/demographics"

demographics_request_body = None
auth_request_body = None
access_token = None


@events.test_start.add_listener
def test_start(environment):
    global demographics_request_body
    global auth_request_body
    global access_token

    if isinstance(environment.runner, MasterRunner):
        # in a distributed run, the master does not typically need any test data
        return

    # read the sample request body for the demographics endpoint
    with open("e2e/src/test/resources/newborn_patient.json", "r") as f:
        demographics_request_body = f.read()

    # set up the sample request body for the auth endpoint
    # using a valid test token found in the mock_credentials directory
    auth_scope = "report-stream"
    with open("mock_credentials/report-stream-valid-token.jwt") as f:
        auth_token = f.read()

    auth_request_body = urllib.parse.urlencode(
        {"scope": auth_scope, "client_assertion": auth_token}
    )

    # get a valid bearer token from the auth endpoint
    request = urllib.request.Request(
        f"{environment.host}{AUTH_ENDPOINT}", data=auth_request_body.encode("utf-8")
    )
    response = urllib.request.urlopen(request)
    data = json.load(response)
    access_token = data["access_token"]


class SampleUser(HttpUser):
    # Each task gets called randomly, but the number next to '@task' denotes
    # how many more times that task will get called than other tasks

    @task
    def get_health(self):
        self.client.get(HEALTH_ENDPOINT)

    @task(5)  # this task will get called 5x more than the other
    def post_v1_etor_demographics(self):
        self.client.post(
            DEMOGRAPHICS_ENDPOINT,
            data=demographics_request_body,
            headers={"Authorization": access_token},
        )

    @task
    def post_v1_auth(self):
        self.client.post(AUTH_ENDPOINT, data=auth_request_body)


@events.quitting.add_listener
def assert_stats(environment):
    if environment.stats.total.fail_ratio > 0.01:
        print("Test failed due to failure ratio > 1%")
        environment.process_exit_code = 1
    elif environment.stats.total.get_response_time_percentile(0.95) > 1000:
        print("Test failed due to 95th percentile response time > 1000 ms")
        environment.process_exit_code = 1
