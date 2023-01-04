import time
from locust import HttpUser, task


class SampleUser(HttpUser):

    # Each task gets called randomly, but the number next to '@task' denotes
    # how many more times that task will get called than other tasks

    @task
    def latency_get_health(self):
        MAX_TIME = 1.000
        OVER_ONE_SECOND = "Response took longer than 1000 ms"
        with self.client.get("/health", catch_response=True) as response:
            if response.elapsed.total_seconds() < MAX_TIME:
                response.success()
            elif response.elapsed.total_seconds() >= MAX_TIME:
                response.failure(OVER_ONE_SECOND)

    @task(5)  # this task will get called 5x more than the other
    # this task will get called 5x more than the other
    def latency_post_v1_etor_orders(self):
        MAX_TIME = 1.000
        OVER_ONE_SECOND = "Response took longer than 1000 ms"
        with self.client.post("/v1/etor/order", catch_response=True) as response:
            if response.elapsed.total_seconds() < MAX_TIME:
                response.success()
            elif response.elapsed.total_seconds() >= MAX_TIME:
                response.failure(OVER_ONE_SECOND)
