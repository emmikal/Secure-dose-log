from models import (
    DurationRange,
    RouteDuration,
    Substance,
)


def duration_to_minutes(value: float | None, units: str | None) -> int | None:
    """
    Convert a duration value into minutes.

    Supported units:
        - minutes
        - hours

    Returns None if the value is missing.
    """

    if value is None:
        return None

    if units == "minutes":
        return round(value)

    if units == "hours":
        return round(value * 60)

    raise ValueError(f"Unsupported duration unit: {units}")


def parse_duration_range(data: dict | None) -> DurationRange | None:
    """
    Convert a GraphQL DurationRange object into our internal model.
    """

    if data is None:
        return None

    return DurationRange(
        min_minutes=duration_to_minutes(
            data.get("min"),
            data.get("units"),
        ),
        max_minutes=duration_to_minutes(
            data.get("max"),
            data.get("units"),
        ),
    )


def parse_route(route: dict) -> RouteDuration:
    """
    Convert one Route of Administration.
    """

    duration = route.get("duration", {})

    return RouteDuration(
        route=route["name"],

        onset=parse_duration_range(duration.get("onset")),
        comeup=parse_duration_range(duration.get("comeup")),
        peak=parse_duration_range(duration.get("peak")),
        offset=parse_duration_range(duration.get("offset")),
        total=parse_duration_range(duration.get("total")),
        afterglow=parse_duration_range(duration.get("afterglow")),
    )


def parse_substance(data: dict) -> Substance:
    """
    Convert one GraphQL Substance into our internal model.
    """

    aliases = list(data.get("commonNames") or [])

    systematic = data.get("systematicName")
    if systematic and systematic not in aliases:
        aliases.append(systematic)

    return Substance(
        name=data["name"],
        aliases=aliases,
        systematic_name=systematic,
        routes=[
            parse_route(route)
            for route in data.get("roas", [])
        ],
    )


def parse_substances(data: list[dict]) -> list[Substance]:
    """
    Convert an entire GraphQL response into Substance models.
    """

    return [parse_substance(item) for item in data]