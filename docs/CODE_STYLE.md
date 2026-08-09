# Code Style Guide

Conventions for CSC207-MarketMaker. Keep it consistent — that matters more than any single rule.

## Comments

Space between `//` and the text.

```java
// good: adds a position to holdings
//bad: no space
```

## Methods

Simple methods (getters, one-liners) go on a single line, no blank lines between them:

```java
public String getId() { return id; }
public String getTicker() { return ticker; }
public int getQuantity() { return quantity; }
```

Complex methods get a blank line above and below, with a body on its own lines:

```java
public void fill(double fillPrice, Instant filledAt) {
    this.fillPrice = fillPrice;
    this.filledAt = filledAt;
    this.status = Status.FILLED;
}
```

No stray blank lines inside a block of getters.

## Naming (Java convention)

- Fields and methods: `camelCase` — `userBalance`, `editBalance()`, not `UserBalance` / `edit_balance`.
- Classes: `PascalCase` — `Account`, `Order`.
- Constants: `UPPER_SNAKE_CASE`.

```java
private double userBalance;          // field
public double editBalance(double d)  // method
```

## Whitespace

No trailing blank line before the closing brace. Classes should end:

```java
    }
}
```

## Before you push

- Code compiles (`mvn compile`) — a missing semicolon fails the whole build.
- Tests pass (`mvn test`).

Anyone can propose changes to these standards — raise it and we'll agree together.
