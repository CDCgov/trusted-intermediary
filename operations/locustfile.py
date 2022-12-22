from locust import HttpUser, task, between

class SampleUser(HttpUser):

    wait_time = between(1, 5)

    # Each task gets called randomly, but the number next to task denotes
    # how many more times that task will get called
    @task
    def hello_health(self):
        self.client.get("/health")

    @task(5)  # this task will get called 5x more than the other
    def hello_etor(self):
        self.client.post("/v1/etor/order")
