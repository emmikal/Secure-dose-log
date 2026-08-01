import json

from psychonautwiki import PsychonautWikiClient


def main():
    print("Connecting to Bifrost...")

    client = PsychonautWikiClient()

    result = client.query("""
{
  substances(limit: 5, offset: 0) {
    name
    commonNames
    systematicName

    roas {
      name

      duration {

        onset {
          min
          max
          units
        }

        comeup {
          min
          max
          units
        }

        peak {
          min
          max
          units
        }

        offset {
          min
          max
          units
        }

        total {
          min
          max
          units
        }

        afterglow {
          min
          max
          units
        }
      }
    }
  }
}
""")

    print("✓ Connected")
    print()
    print(json.dumps(result, indent=2))


if __name__ == "__main__":
    main()