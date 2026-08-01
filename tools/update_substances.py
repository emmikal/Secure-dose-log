from pathlib import Path

from converter import parse_substances
from generator import generate_database, generate_attribution
from psychonautwiki import PsychonautWikiClient


QUERY = """
{
  substances(limit: 500, offset: 0) {
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
"""

# tools/update_substances.py -> project root is one level up
PROJECT_ROOT = Path(__file__).resolve().parent.parent


def main():

    print("Connecting to Bifrost...")

    client = PsychonautWikiClient()

    result = client.query(QUERY)

    substances = parse_substances(result["substances"])

    print(f"✓ Loaded {len(substances)} substances")

    output_dir = PROJECT_ROOT / "app" / "src" / "main" / "assets"
    output_dir.mkdir(exist_ok=True, parents=True)

    json_output = output_dir / "substances.json"
    json_output.write_text(
        generate_database(substances),
        encoding="utf-8",
    )
    print(f"✓ Wrote {json_output}")

    attribution_output = output_dir / "substances_ATTRIBUTION.txt"
    attribution_output.write_text(
        generate_attribution(),
        encoding="utf-8",
    )
    print(f"✓ Wrote {attribution_output}")


if __name__ == "__main__":
    main()