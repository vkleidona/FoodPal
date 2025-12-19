| Key            | Value                                                                                       |
|----------------|---------------------------------------------------------------------------------------------|
| Date :         | 18-12-2025                                                                                  |
| Time :         | 14:45-15:30                                                                                 |
| Location :     | DW PC Hall 1                                                                                |
| Chair :        | V. Kleidona                                                                                 |
| Minute Taker : | A. Iliev                                                                                    |
| Attendees :    | K. Kadrieh, A. Iliev, V. Kleidona, T. Corlateanu, E. Tskhay (TA)                            |

## Agenda Items :

<br>

### Opening

<br>

#### Opening by chair (estimated: 1 min | actual: 1 min)

<br>

#### Check-in : (estimated: 2 min | actual: 1 min)
 - How is everyone doing? - Everyone is doing fine, there were no particular remarks.

<br>

#### Any potential important announcements by the team (estimated: 1 min | actual: 2 min)

<br>

<u>Problems with communicating with some team members</u>:
- The team raised the issue that Max hasn't been responding to all communication forms we have tried and has not contributed any code, tests or any kind of input in the last 2 weeks
- The team also brought up that we haven't heard from Pawel in the last week, and that he has not contributed in any way for this week
- the TA will bring up the issue to the relevant administrators

<br>

#### Approval of the agenda (estimated: 1 min | actual: 1 min)
 - Does anyone have any additions?  - The agenda was approved, no additions were made.

<br>

#### Approval of last minutes (estimated: 1 min | actual: 1 min)
 - Did everyone read the last minutes and do you approve these? - Everybody (present) has read the minutes from last meeting, everything was approved.

<br>

#### Announcements by the TA (estimated: 2 min | actual: 4 min)

<u>Formative feedback uploaded:</u>

- The TA has uploaded the first two aspects of formative feedback - on Tasks & Planning and Code Contribution & Code Reviews
    - the team is on track, although we need to improve some aspects

- The TA will upload the formative feedback on the Technology aspect by the end of this week (by 21.12.2025)

- The team's grades for the Buddycheck assignment will be released soon (in the upcoming hours, on 18.12.2025).

<br>

### Progress update

<br>

#### Individual progress update (estimated: 6 min, 1 min each | actual: 8 min)
- Short description of each members' weekly tasks
- What did everyone manage to complete? / What must be done next week?
- Discussion of potential problems/difficulties we encountered

<br>

<u>Vasiliki</u>:
- took over Max's tasks - fixed the Edit Ingredients UI
- the Download Recipe feature - ready to be merged
- has experienced merge conflicts and failures on the pipeline - these will be fixed by tomorrow
- currently implementing the drop down menu for the Food Item

<br>

<u>Aleksandar</u>:
- Add Recipe to Shopping List feature is ready and merged
    - with that, the Shopping List Epic is essentially complete 
- Currently working on the implementation of both UIs for the Shopping list to jump to the Add Food Item UI when the food item entered does not exist - will implement by the end of the day (18-12-2025)
- will try to add tests for both UI classes by tomorrow (19-12-2025), otherwise will continue during the break

<br>

<u>Khaled</u>:
- Food Items are now ordered by name alphabetically in the Food Item UI, implementing a user story
- Implementing the user story of changing the name of a Food Item, as now it causes issues
    - has run into a problem - the name cannot be changed, as it currently functions as the FoodItem entity's primary key
    - will change the Food Item primary key to an automatically generated ID (and make the name unique) - will cause a lot of changes
- planned to add tests this week, will try to do so by tomorrow (19-12-2025)

<br>

<u>Teodora</u>:
- has implemented a dropdown menu for the Ingredient's food type in the Add Recipe menu (meeting a requirement from a user story)
- has implemented the servings attribute for each recipe
- has implemented the scaling of a recipe
- is going to test her additions to the UI - maybe next week
- had issues with understanding the last bullet point of the requirements for the Nutritional values (about the scaled Nutritional values for the whole recipe) - brought up the topic for discussion

<br>

<u>Discussion of the nutritional values implementation</u>:
- Summary: After discussing and consulting with the TA, the team decided that the nutritional values (scaled for each recipe) should be shown both in total and per 100 g.
    - The total nutritional values should also be implemented. 
- The discussion was continued during the presentation of the app to the TA and after all issues on the agenda were discussed
- The TA will also contact a supervisor to get more detailed information about this topic

<br>


#### Presentation of the current app to TA (estimated: 3 min | actual: 3 min)

All new features of the app were presented, including:
- the linking of the Main UI to the Edit Instructions UI and Food Item UI - also the functionality of these UIs
- the Information panel for each recipe
- the Add Recipe to Shopping List feature

<br>

### Current issues for discussion: (20 min)

<br>

#### Christmas break (estimated: 3 min | actual: 2 min)

<br>

<u>Any plans on working during the break?</u>
- Aleksandar said he will complete the testing and his other tasks for his week in the Christmas break if he cannot implement them by time
- Aleksandar also said he might work on a new feature - the team did not oppose that notion
- the team decided that working during the Christmas break was not mandatory, but the option still remained

<br>

<u>Communication during the Christmas break?</u>
- the team decided to write in the WhatsApp group chat for any updates / features they might be working on, as well as about any merge requests

<br><br>

#### Approvals needed for a merge request (estimated: 5 min | actual: 3 min)

<br>

<u>Implementing the actual change for merge requests:</u>
- Vasiliki asked whether we should actually enforce the 2 approvals per merge requests rule from our Code of Conduct in GitLab
- Aleksandar mentioned that he tried doing that after the decisions of the last meeting, but he wasn't successful due to the team not having enough rights to change the project structure
    - the TA agreed that we probably don't have enough rights to do that - but he could ask the administrators whether they could change that rule for our group specifically
- the team agreed to keep that rule as just an agreement
- Aleksandar brought up whether we should revert back to 1 approval per merge request considering that the team has been reduced to 4 members
    - the TA agreed that that is probably a sensible option

<br><br>

#### Example of how to test JavaFX components added on BrightSpace (estimated: 2 min | actual: 1 min)

<u>Testing FXML Components</u>:
- Vasiliki brought up that an example project of how to test FXML has been recently uploaded to Brightspace and asked whether we should implement these tests into our project
    - Khaled mentioned he hasn't tested his controller classes yet, so the tests can be implemented using the example project
- the team agreed that the new tests should follow the example set by the project, and the old ones don't need to be changed

<br><br>

#### Creation of new milestones (estimated: 4 min | actual: 2 min)

<u>Creating new milestones and issues:</u>
- Aleksandar mentioned he can create the new milestones for the week
    - Khaled asked whether we should plan all of our milestones until the end of the project from now - the team did not reach a conclusion
- After some discussion, the team agreed that everyone should implement their own issues, including time management and tags

<br>

#### Future tasks (estimated: 6 min | actual: 1 min)

Khaled and Vasiliki suggested that the detailed task distribution for the next week be done on the Monday after the break, and the team agreed unanimously.

<br><br>

### Additional topics for discussion

<br>

#### Discussion on Nutritional Values (estimated: 0 min | actual: 5 min):

<br>

<u>Total and relative nutritional values:</u>
- Teodora asked whether the recipes should display both total and relative nutritional values, or only one of the two
- After some analysis and discussion, the TA and Teodora (with some participation from the rest of the team) agreed to represent both in the UI

<u>Scaling of servings by ingredients:</u>
- Aleksandar asked whether a scaling should also apply from the ingredients of a recipe to the servings
- After a discussion, the team and the TA decided it was more logical that the servings should scale the ingredients, but the ingredients should not scale the servings
    - In any case, the TA will ask for clarification on that matter

<br>

#### Further announcements by the TA (estimated: 0 min | actual: 8 min):

<br>

<u>Feedback on Tasks & Planning</u>:
- positive feedback:
    - Weekly milestones, split into multiple epics is an excellent approach.
    - Issues were reasonably sized (3-4 h).
    - The titles of issues and milestones were good.
- potential for improvement:
    - issues (especially the descriptions) were sometimes a little too vague
        - The descriptions need to be longer and contain what exactly needs to be done for an Excellent grade.
        - Time estimations should be added for each issue.
        - The team should then update the issues with the actual time spent on them.
    - Issues were copies of the backlog user stories - the team should split these user stories into more issues.
    - In the issues, the team should clearly reference which user story they are currently fulfilling.
    - labels - The team should add more labels.
        - The TA suggested the team adds bug labels, priority labels, a user story label, etc.

<br>

<u>Code contribution</u>:
- positive feedback:
    - Commits are focused - file changes per commit should be around 3, which is the case.
    - lines per commit - the average lines per commit is good, but can be improved
        - The average should be around 100 (the current average is 140) - which is close, but there is room for some improvement.
    - File changes should be around 3, which is the case.
    - Merge requests are accepted reasonably quickly.
    - Merge requests should have around 10 commits - the current average is close to that.
        - Some merge requests have too many commits (a maximum of 27).
- room for improvement:
    - Merge requests are typically considerably behind the main branch - the team should pull from main the latest changes
    - Merge requests should be more specific for a Good grade
    - Team members should hold discussions for each merge request - with suggestions, questions, clarifications which lead to iterative improvements.
        - Iterative improvements on each merge request are required for an Excellent grade
- The team should write more tests on the code they implement - preferably in the same merge request
    - The team should write tests while implementing the features, as that is a requirement for an Excellent grade.

<br>

Tip: Team members should manage how the team is doing for all criteria in the grading rubrics to improve our current grades and achieve Excellent.

<br>

### Summary

<br>

#### Summarize action points: (estimated: 3 min | actual: 1 min)

<br>

#### Question round (estimated: 2 min | actual: 1 min): 
- Does anyone have anything to add before the meeting closes? - No additions were made.

<br>

#### Feedback round (estimated: 1 min | actual: 1 min)
- What went well and what can be improved next time? - Teodora thought the meeting went well, and the others agreed.

<br>

#### Meeting duration vs Actual duration? (estimated: 1 min | actual: 1 min)
- planned duration: 44 min
- actual duration: 49 min

<br>

#### Closure (estimated: 1 min | actual: 1 min)

The TA will ask the staff for more clarification details on the team's questions about the nutritional values, as well as our other concerns - in the coming hours (on 18-12-2025), the team will be updated.