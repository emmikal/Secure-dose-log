from collections import Counter

from psychonautwiki import PsychonautWikiClient


def normalize(value: str) -> str:
    return value.strip().lower()


def main():
    print("Connecting to Bifrost...")

    client = PsychonautWikiClient()

    result = client.query("""
    {
      substances(limit: 1000, offset: 0) {
        name
        commonNames

        class {
          chemical
          psychoactive
        }

        dangerousInteractions {
          name
        }

        unsafeInteractions {
          name
        }

        uncertainInteractions {
          name
        }
      }
    }
    """)

    substances = result["substances"]

    print(f"✓ Loaded {len(substances)} substances")
    print()

    known = set()

    substance_count = 0
    alias_count = 0
    chemical_class_count = 0
    psychoactive_class_count = 0

    #
    # Build lookup table
    #
    for substance in substances:

        known.add(normalize(substance["name"]))
        substance_count += 1

        for alias in substance.get("commonNames") or []:
            known.add(normalize(alias))
            alias_count += 1

        classes = substance.get("class")

        if classes:

            for chemical in classes.get("chemical") or []:
                known.add(normalize(chemical))
                chemical_class_count += 1

            for psychoactive in classes.get("psychoactive") or []:
                known.add(normalize(psychoactive))
                psychoactive_class_count += 1

    total_targets = 0
    resolved_targets = 0

    unknown = Counter()

    #
    # Scan interaction graph
    #
    for substance in substances:

        for field in (
            "dangerousInteractions",
            "unsafeInteractions",
            "uncertainInteractions",
        ):

            interactions = substance.get(field) or []

            for interaction in interactions:

                total_targets += 1

                name = interaction["name"]

                if normalize(name) in known:
                    resolved_targets += 1
                else:
                    unknown[name] += 1

    print("Known identifiers")
    print("-----------------")
    print(f"Substances:          {substance_count}")
    print(f"Aliases:             {alias_count}")
    print(f"Chemical classes:    {chemical_class_count}")
    print(f"Psychoactive classes:{psychoactive_class_count}")
    print()

    print("Interaction targets")
    print("-------------------")
    print(f"Total:               {total_targets}")
    print(f"Resolved:            {resolved_targets}")
    print(f"Unknown:             {sum(unknown.values())}")
    print()

    if unknown:

        print("Unknown interaction targets")
        print("---------------------------")

        for name, count in unknown.most_common():
            print(f"{count:4}  {name}")

    else:
        print("Every interaction target resolved successfully! 🎉")


if __name__ == "__main__":
    main()