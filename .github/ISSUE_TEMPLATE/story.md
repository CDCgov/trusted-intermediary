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
    - [ ] Implementation guide (`/ig` folder)
    - [ ] ADRs (`/adr` folder)
    - [ ] Main README.md
    - [ ] Other READMEs in the repo
  - [ ] Threat model updated
  - [ ] API documentation generated
  - [ ] Source code documentation created when the code is not self-documenting
  - [ ] Logging added where useful
- [ ] Code quality tasks completed
  - [ ] Code refactored for clarity and no design/technical debt
  - [ ] Adhere to separation of concerns; code is not tightly coupled, especially to 3rd party dependencies
  - [ ] Code is reviewed or developed by pair; 1 approval is needed but consider requiring an outside-the-pair reviewer
  - [ ] Code quality checks passed
- [ ] Security & Privacy tasks completed
  - [ ] Security & privacy gates passed
- [ ] Testing tasks completed
  - [ ] Unit test coverage of our code >= 90%
  - [ ] Load tests passed
- [ ] Build & Deploy tasks completed
  - [ ] Source code is merged to the main branch
  - [ ] Build process updated
  - [ ] Feature toggles created and/or deleted.  Document the feature toggle
  - [ ] API(s) are versioned

## Research Questions
- *Optional: Any initial questions for research*

## Decisions
- *Optional: Any decisions we've made while working on this story*

## Notes
- *Optional: Any reference material or thoughts we may need for later reference*
