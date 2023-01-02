import time
from locust import HttpUser, task


class SampleUser(HttpUser):

    # Each task gets called randomly, but the number next to '@task' denotes
    # how many more times that task will get called than other tasks
    @task
    def check_response_health(self):
        with self.client.get("/health", catch_response=True) as response:
            if response.text != "Operational":
                response.failure("Unexpected response: " + response.text)

    @task
    def latency_get_health(self):
        MAX_SPEED = 1.000
        OVER_ONE_SECOND = "Response took longer than 1000 ms"
        with self.client.get("/health", catch_response=True) as response:
            if response.elapsed.total_seconds() < MAX_SPEED:
                response.success()
            elif response.elapsed.total_seconds() >= MAX_SPEED:
                response.failure(OVER_ONE_SECOND)

    @task
    def check_response_v1_etor_orders(self):
        with self.client.post("/v1/etor/order", catch_response=True) as response:
            if response.text != "DogCow sent in a lab order":
                response.failure("Unexpected response: " + response.text)

    @task(5)  # this task will get called 5x more than the other
    # this task will get called 5x more than the other
    def latency_post_v1_etor_orders(self):
        MAX_SPEED = 1.000
        OVER_ONE_SECOND = "Response took longer than 1000 ms"
        with self.client.post("/v1/etor/order", catch_response=True) as response:
            if response.elapsed.total_seconds() < MAX_SPEED:
                response.success()
            elif response.elapsed.total_seconds() >= MAX_SPEED:
                response.failure(OVER_ONE_SECOND)
