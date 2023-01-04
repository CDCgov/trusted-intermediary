from locust import HttpUser, task, events


class SampleUser(HttpUser):

    # Each task gets called randomly, but the number next to '@task' denotes
    # how many more times that task will get called than other tasks

    @task
    def latency_get_health(self):
        self.client.get("/health")

    @task(5)  # this task will get called 5x more than the other
    # this task will get called 5x more than the other
    def latency_post_v1_etor_orders(self):
        self.client.post("/v1/etor/order", json={
            "id": "asdf-12341-jkl-7890",
            "destination": "Massachusetts",
            "createdAt": "2022-12-21T08:34:27Z",
            "client": "MassGeneral",
        })


@events.quitting.add_listener
def assert_latency_stats(environment):
    if environment.stats.total.fail_ratio > 0.01:
        print("Test failed due to failure ratio > 1%")
        environment.process_exit_code = 1
    elif environment.stats.total.get_response_time_percentile(0.95) > 1000:
        print("Test failed due to 95th percentile response time > 1000 ms")
        environment.process_exit_code = 1
