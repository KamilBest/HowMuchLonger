# Testing Approach for DurationFormatter

## Overview
This document explains the testing approach used for the `DurationFormatter` class, which is a key component in the countdown functionality of the app.

## Challenges
The main challenge in testing `DurationFormatter` is that it uses Android-specific classes (`MeasureFormat`, `Measure`, `MeasureUnit`) that are not available in the JVM environment where unit tests run. This makes it difficult to test the actual implementation directly.

## Solution: Test Double
To overcome this challenge, we created a test double (`DurationFormatterTestDouble`) that mimics the behavior of the actual `DurationFormatter` but doesn't rely on Android-specific classes. This allows us to test the formatting logic in a JVM environment.

The test double implements the same interface as the actual `DurationFormatter` and produces the same output for the same input, but uses standard Java classes for formatting.

## Tests
The tests for `DurationFormatter` cover various scenarios:
1. Basic formatting for different durations (seconds, minutes, hours, days)
2. Handling of non-positive durations (returning "EVENT PAST")
3. Control of seconds display with the `showSeconds` parameter
4. Transitions between time units (days to hours, hours to minutes, minutes to seconds)
5. Edge cases like very small values and countdown approaching zero

These tests ensure that the formatting logic works correctly for all possible inputs, which is crucial for the countdown functionality to work properly.

## Why Not Instrumented Tests?
While it would be possible to test the actual `DurationFormatter` implementation using instrumented tests (which run on an Android device or emulator), we chose to use unit tests with a test double for several reasons:
1. Unit tests are faster to run than instrumented tests
2. Unit tests don't require an Android device or emulator
3. The formatting logic is pure business logic that doesn't depend on Android-specific behavior, so it can be tested independently

## Limitations
The main limitation of this approach is that we're not testing the actual implementation of `DurationFormatter`, but rather a test double that mimics its behavior. This means that if there are bugs in the actual implementation that aren't present in the test double, our tests won't catch them.

However, since the formatting logic is relatively simple and the test double closely mirrors the actual implementation, this risk is minimal.