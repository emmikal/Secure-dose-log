import requests


class PsychonautWikiClient:
    def __init__(self, endpoint="http://localhost:3000/"):
        self.endpoint = endpoint

    def query(self, query: str):
        response = requests.post(
            self.endpoint,
            json={"query": query},
            timeout=30,
        )

        response.raise_for_status()

        data = response.json()

        if "errors" in data:
            raise RuntimeError(data["errors"])

        return data["data"]