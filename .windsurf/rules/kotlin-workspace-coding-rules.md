---
trigger: always_on
---

# Kotlin Language Rules

## Overview
You are an expert in Kotlin programming and related JVM technologies.

## Kotlin-Specific Best Practices
- Prefer val over var to create immutable references
- Utilize Kotlin's null safety features to prevent null pointer exceptions
- Use data classes for DTOs and immutable data structures
- Leverage Kotlin's extension functions to enhance existing classes without inheritance
- Use sealed classes for representing restricted class hierarchies
- Implement Kotlin's scope functions (let, apply, run, with, also) appropriately
- Leverage Kotlin's coroutines for asynchronous programming
- Use constructor-based dependency injection
- Use destructuring declarations where appropriate
- Prefer immutable collections when possible
- Use sequence for large collection processing
- Utilize inline functions for higher-order functions
- Use reified type parameters when needed
- Leverage delegation pattern with 'by' keyword
- Use companion objects appropriately
- Implement operator overloading judiciously

## Naming Conventions
- Use PascalCase for class names (e.g., UserController, OrderService)
- Use camelCase for method and variable names (e.g., findUserById, isOrderValid)
- Use ALL_CAPS for constants (e.g., MAX_RETRY_ATTEMPTS, DEFAULT_PAGE_SIZE)
- Use meaningful and descriptive names that reflect purpose
- Prefix interface names with 'I' only when there's a class with the same name
- Use verb phrases for function names (e.g., calculateTotal, processPayment)
- Use noun phrases for properties and variables (e.g., userName, orderTotal)

## Code Structure
- Keep functions small and focused on a single responsibility
- Limit function parameters (consider using data classes for multiple parameters)
- Use expression bodies for simple functions
- Organize code with extensions in separate files by receiver type
- Group related properties and functions together
- Use proper package structure to organize code
- Follow SOLID principles to ensure code is maintainable and extensible

## Kotlin Coroutines
- Use the appropriate dispatcher (IO for I/O operations, Default for CPU-intensive tasks)
- Properly handle exceptions in coroutines with try-catch or supervisorScope
- Use structured concurrency with coroutineScope
- Cancel coroutines when no longer needed
- Use Flow for asynchronous streams of data
- Apply backpressure with buffer, conflate, or collectLatest when needed
- Use withContext for changing context without creating new coroutines
- Avoid using runBlocking in production code
- Use async/await for parallel decomposition

## Kotlin Coroutines Testing
- Use runTest for testing suspend functions in unit tests
- Properly wrap assertions that test suspending functions with runBlocking when needed
- Ensure mock setup is compatible with coroutine execution
- Always mock ALL method calls that will occur during test execution, including chained calls
- For each mocked method that returns a value used later in the service, explicitly define behavior with `every { ... } returns ...`
- Verify all important mock interactions with `verify(exactly = n) { ... }`
