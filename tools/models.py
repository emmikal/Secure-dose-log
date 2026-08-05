from dataclasses import dataclass


@dataclass
class DurationRange:
    min_minutes: int | None
    max_minutes: int | None


@dataclass
class RouteDuration:
    route: str

    onset: DurationRange | None
    comeup: DurationRange | None
    peak: DurationRange | None
    offset: DurationRange | None
    total: DurationRange | None
    afterglow: DurationRange | None


@dataclass
class Substance:
    name: str
    aliases: list[str]
    systematic_name: str | None

    chemical_classes: list[str]
    psychoactive_classes: list[str]

    dangerous_interactions: list[str]
    unsafe_interactions: list[str]
    uncertain_interactions: list[str]

    routes: list[RouteDuration]