import json

from psychonautwiki import PsychonautWikiClient

client = PsychonautWikiClient()

result = client.query("""
{
  substances(query: "Grapefruit", limit: 1) {
    name
  }
}
""")

print(json.dumps(result, indent=2))