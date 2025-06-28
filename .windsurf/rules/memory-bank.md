---
trigger: always_on
---

# Memory Bank Setup Rules

---

## Directory Structure

memory-bank/
├── projectVision.md
├── projectbrief.md
├── productContext.md
├── activeContext.md
├── systemPatterns.md
├── techContext.md
└── progress.md

## File Content & Guidelines
Each file serves a distinct purpose with strict content rules, ensuring clarity and preventing overlap.
* **`projectVision.md`**: Defines **mission, vision, strategic goals, timelines, differentiators, impact, and success metrics**. Focuses on aspirational outcomes and strategic direction; **no technical details**. Updates when strategic direction, market conditions, or metrics change.

* **`projectbrief.md`**: Outlines **technical requirements, stack, project structure, setup, timeline, and phases**. Focuses on user needs and business value; **no visionary statements or technical solutions**. Updates for new features, scope, or technology stack changes.

* **`productContext.md`**: Addresses **problem statements, market analysis, user needs, experience goals, and feature requirements**. Focuses on user needs and business value; **no technical solutions**. Updates for new features, scope, or technology stack changes.

* **`activeContext.md`**: Captures **current focus, priorities, recent changes, decisions, and next steps/action items**. Focuses on actual work status; **no historical decisions or speculation**. Updates daily for status, completed work, and metrics.

* **`systemPatterns.md`**: Documents **architecture patterns, decisions, component relationships, and integration patterns**. Focuses on system design; **no implementation details or premature specifics**. Updates when design patterns, boundaries, or dependencies shift.

* **`techContext.md`**: Specifies **technology stack and versions, development setup, prerequisites, and infrastructure configuration**. Focuses on agreed-upon technology with clear justification; **no business requirements or assumptions**. Updates when dependencies, tools, or scaling requirements change.

* **`progress.md`**: Reports **current status, metrics, completed work, and issues**. Focuses on verifiable metrics; **no future plans or speculation**. Updates daily for status, completed work, and metrics.

### Core Documentation Files

* **`projectVision.md`**: Defines **mission, vision, strategic goals, timelines, differentiators, impact, and success metrics**.

* **`projectBrief.md`**: Outlines **technical requirements, stack, project structure, setup, timeline, and phases**.

* **`productContext.md`**: Addresses **problem statements, market analysis, user needs, experience goals, and feature requirements**.

* **`activeContext.md`**: Captures **current focus, priorities, recent changes, decisions, and next steps/action items**.

* **`systemPatterns.md`**: Documents **architecture patterns, decisions, component relationships, and integration patterns**.

* **`techContext.md`**: Specifies **technology stack and versions, development setup, prerequisites, and infrastructure configuration**.

* **`progress.md`**: Reports **current status, metrics, completed work, and issues**.


### Additional Documentation Files

* **`successCriteria.md`**: **MUST** contain measurable business outcomes, KPIs, acceptance criteria. **MUST NOT** contain technical metrics.

* **`userPersonas.md`**: **MUST** contain user characteristics, needs, pain points. **MUST NOT** contain technical user requirements.

* **`componentDesign.md`**: **MUST** contain logical component structure and relationships. **MUST NOT** contain specific technology implementations or code details.

* **`dataModels.md`**: **MUST** contain logical data structures and relationships. **MUST NOT** contain specific database technologies.

* **`apiContracts.md`**: **MUST** contain business requirements for APIs and data flow. **MUST NOT** contain specific implementation technologies.

* **`testingStrategy.md`**: **MUST** contain agreed-upon testing approaches and requirements. **MUST NOT** contain specific tool implementations.

* **`decisionLog.md`**: **MUST** contain documented architectural and technical decisions. **MUST NOT** contain assumptions.

* **`openQuestions.md`**: **MUST** contain documented uncertainties and research requirements. **MUST NOT** contain premature solutions.

General Content Guidelines
* **Vision Files**: Business value, strategic outcomes, quantifiable metrics. No technical assumptions.
* **Requirements Files**: Strictly business-focused, no technical solutions. Validated with stakeholders.
* **Architecture Files**: High-level design, based on requirements. No premature implementation details.
* **Technical Files**: Only agreed-upon decisions with clear justification. No assumptions.
* **Tracking Files**: Document actual decisions and progress. Verifiable.

## Setup & Maintenance

### Setup Principles

* **Complete Traceability**: All changes link to requirements.
* **Living Documentation**: Evolves with the project.
* **Single Source of Truth**: Consolidated knowledge.
* **Context Preservation**: Document decisions and reasoning.
* **Knowledge Transfer**: Enables quick team onboarding.
* **Continuous Reflection**: Regular review and refinement.

---

### File Templates & Formatting

Each file **MUST** include:

* **Header** with creation date and purpose.
* **Table of contents** for files >500 words.
* Consistent **Markdown** with H2/H3 hierarchy.
* **Cross-reference links** using relative paths.
* **Status indicators**: `[DRAFT]`, `[REVIEW]`, `[APPROVED]`.
* **Author attribution** and last modified timestamp.

---

### Initialization Checklist

* Create directory structure exactly.
* Initialize each file with required sections.
* Set up Git with semantic versioning.
* Configure cross-reference linking.
* Add file headers with metadata.
* Populate initial content based on project.
* Establish review and approval workflows.
* Create backup and versioning strategy.
* Document file ownership/responsibilities.
* Schedule regular review cycles.