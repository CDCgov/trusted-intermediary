import time
from locust import HttpUser, User, task, constant


class SampleUser(HttpUser):

    # Each task gets called randomly, but the number next to task denotes
    # how many more times that task will get called
    @task
    def get_health_response(self):
        with self.client.get("/health", catch_response=True) as response:
            if response.text != "Operational":
                response.failure("Unexpected response: " + response.text)

    @task
    def health_latency(self):
        with self.client.get("/health", catch_response=True) as response:
            if response.elapsed.total_seconds() < 1:
                response.success()
            elif response.elapsed.total_seconds() > 1.001:
                response.failure("Response took longer than 1000 ms")

    @task  # this task will get called 5x more than the other
    def post_v1_etor_order_response(self):
        with self.client.post("/v1/etor/order", catch_response=True) as response:
            if response.text != "DogCow sent in a lab order":
                response.failure("Unexpected response: " + response.text)

    @task  # this task will get called 5x more than the other
    def hello_etor(self):
        self.client.post("/v1/etor/order")
