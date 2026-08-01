from models import (
    DurationRange,
    RouteDuration,
    Substance,
)


def duration_to_minutes(value: float | None, units: str | None) -> int | None:
    """
    Convert a duration into minutes.

    Supported units:
        - seconds
        - minutes
        - hours
        - days
    """

    if value is None:
        return None

    normalized_units = units.lower() if units else None

    if units == "seconds":
        return round(value / 60)

    if normalized_units == "minutes":
        return round(value)

    if normalized_units == "hours":
        return round(value * 60)

    if normalized_units == "days":
        return round(value * 60 * 24)

    raise ValueError(f"Unsupported duration unit: {units}")


def parse_duration_range(data: dict | None) -> DurationRange | None:
    """
    Convert a GraphQL DurationRange object.
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

    duration = route.get("duration") or {}

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
    Convert one GraphQL Substance.
    """

    aliases = list(data.get("commonNames") or [])

    systematic_name = data.get("systematicName")
    if systematic_name and systematic_name not in aliases:
        aliases.append(systematic_name)

    return Substance(
        name=data["name"],
        aliases=aliases,
        systematic_name=systematic_name,
        routes=[
            parse_route(route)
            for route in (data.get("roas") or [])
        ],
    )


def parse_substances(data: list[dict]) -> list[Substance]:
    """
    Convert an entire GraphQL response.
    """

    return [
        parse_substance(substance)
        for substance in data
    ]