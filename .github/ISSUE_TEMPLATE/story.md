---
name: Story
about: Suggest a user story for this product
title: ''
labels: story
assignees: ''

---

# Story
As a _, so that _, I need _.

## Pre-conditions
- [ ] *Assumptions of prior or future work that's out of scope for this story*

## Acceptance Criteria
- [ ] *Required outcomes of the story*

## Tasks

### Research
- [ ] *Research work needed to complete the story*
- [ ] Foundational: *High-level research that will support this and future efforts*

###  Engineering
- [ ] *Engineering work needed to complete the story*
- [ ] Foundational: *Technical runway work to support this and future efforts*

## Definition of Done
- [ ] Documentation tasks completed
  - [ ] Documentation and diagrams created or updated
    - [ ] ADRs (`/adr` folder)
    - [ ] Main `README.md`
    - [ ] Other READMEs in the repo
    - [ ] If applicable, update the `ReportStream Setup` section in `README.md`
  - [ ] [Threat model](https://lucid.app/lucidchart/8c6e8d37-2612-42a8-ac57-150c83f8e29e/edit) updated
  - [ ] API documentation updated
- [ ] Code quality tasks completed
  - [ ] Code refactored for clarity and no design/technical debt
  - [ ] Adhere to separation of concerns; code is not tightly coupled, especially to 3rd party dependencies
- [ ] Testing tasks completed
  - [ ] Load tests passed
  - [ ] Additional e2e tests created
  - [ ] Additional RS e2e assertions created in the `rs-e2e` project for any new transformations.  Includes improvements to the assertion code required to make the new assertions
- [ ] Build & Deploy tasks completed
  - [ ] Build process updated
  - [ ] API(s) are versioned
  - [ ] Feature toggles created and/or deleted.  Document the feature toggle
  - [ ] Source code is merged to the main branch

## Research Questions
- *Optional: Any initial questions for research*

## Decisions
- *Optional: Any decisions we've made while working on this story*

## Notes
- *Optional: Any reference material or thoughts we may need for later reference*
