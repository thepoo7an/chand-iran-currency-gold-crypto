#!/usr/bin/env python3
"""Shape-check every workflow in ``.github/workflows``.

GitHub only tells you a workflow is broken *after* you push it to the default branch, and an invalid
file shows up as a run named ``.github/workflows/<file>.yml`` with zero jobs — which is what happened
when a ``with:`` block ended up on a ``run`` step and the whole release silently stopped building. This
script makes the same mistakes fail on the pull request instead: it parses every workflow and checks the
step keys GitHub rejects.
"""

import pathlib
import sys

try:
    import yaml
except ImportError:  # pragma: no cover - the CI step installs it
    print("pyyaml is required: python3 -m pip install --break-system-packages pyyaml")
    sys.exit(2)

FAILURES = []


def fail(path, message):
    FAILURES.append(f"{path}: {message}")


def check_steps(path, job_name, steps):
    for index, step in enumerate(steps):
        if not isinstance(step, dict):
            fail(path, f"job {job_name} step {index} is not a mapping")
            continue
        label = step.get("name", f"step {index}")
        if "uses" in step and "run" in step:
            fail(path, f"job {job_name} '{label}' sets both 'uses' and 'run'")
        if "with" in step and "uses" not in step:
            fail(path, f"job {job_name} '{label}' sets 'with' without 'uses'")
        if "run" not in step and "uses" not in step:
            fail(path, f"job {job_name} '{label}' sets neither 'uses' nor 'run'")


def main():
    workflows = sorted(pathlib.Path(".github/workflows").glob("*.y*ml"))
    if not workflows:
        print("no workflows found")
        return 1
    for path in workflows:
        try:
            document = yaml.safe_load(path.read_text())
        except Exception as error:  # noqa: BLE001 - surface the parse error verbatim
            fail(path, f"YAML parse error: {error}")
            continue
        if not isinstance(document, dict) or "jobs" not in document:
            fail(path, "no 'jobs' mapping")
            continue
        for job_name, job in document["jobs"].items():
            if not isinstance(job, dict):
                continue
            check_steps(path, job_name, job.get("steps") or [])
        print(f"ok   {path} ({len(document['jobs'])} job(s))")

    for message in FAILURES:
        print(f"FAIL {message}")
    return 1 if FAILURES else 0


if __name__ == "__main__":
    sys.exit(main())
