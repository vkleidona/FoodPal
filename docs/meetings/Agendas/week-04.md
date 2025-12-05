| Key            | Value                                                                      |
|----------------|----------------------------------------------------------------------------|
| Date :         | 05-12-2025                                                                 |
| Time :         | 14:47-15:33                                                                |
| Location :     | DW Hall 2                                                                  |
| Chair :        | A. Iliev                                                                   |
| Minute Taker : | K. Kadrieh                                                                 |
| Attendees :    | P. Gruca, K. Kadrieh, A. Iliev, V. Kleidona, T. Corlateanu, E. Tskhay (TA) |
Agenda Items :
- Opening by chair (1 min)
- Check-in : (1 min)
    - How are you all doing?
  
---
- Any potential important announcements by the team (1 min)
    - Does anybody want to announce anything?

#### Minutes:

- Teodora and Pawel: Next meeting (after the midterms) they won't be able to attend it in person and instead they will join online.
---

- Approval of the agenda - Does anyone have any additions? (1 min)

---
- Announcements by the TA (2 min)

#### Minutes:

- Gitlab is down Wednesday week 5, Pull repos before then because remote will not be accessible. Git is planned to be back up on 17th of December (Wednesday week 6)
- Week 6, the CSEP lab on Thursday and thus the meeting too.
- Comments/Reviews on MR's: - Should be long enough, - Discussions should be held, - Constructive feedback is expected and ask questions if something is unclear, - Comments with atleast 100 characters on Java code is a requirement every week, otherwise you get a strike, probably must be a single comment with the 100 Characters. Must comment on other's MRs.
- Buddycheck deadline today 23:59, use AID framework which can be found on Brightspace.
- Week 5 no code contributions required, Week 6 there will again be code requirements. Week 5 lines DON'T count for week 6. Saturday onward all count for next week.
- Formative Code Review. Code Review for the whole group as one. Doesn't affect grade but it's there to help you and tell you if you are doing things correctly.
- Buddycheck is anonymous, only TA's and stuff can see.

---
- Progress Update from every team member (9 min, ~1.5 min for each team member)
    - Short description of the planned out tasks for the week
    - Short description of the accomplished tasks for the week
    - Reflection on reasons for potential mismatch between the plan and the implementation
        - Discussion of potential difficulties and pitfalls: (time may vary per each member and their problems)
            - unrealistic time estimations for the tasks
            - unexpected challenges in implementation
            - communication / planning issues

#### Minutes:

- A. Iliev:
  implemented Shopping List, you can add ingredients and print a shopping list, almost requirements corresponding to that UI is finished. Sending a whole recipe to shopping list must still be implemented.

- V. Kleidona:
Wrote Tests, Back and forth connection between the edit UI and Main UI was implemented, modified MainCtrl so we can easily switch scenes in all FXML Controllers.

- M. Tinkelenberg:
Unknown

- T. Corlateanu:
User can choose variety of units, metric, imperial, they also can make their own units, wrote tests for that too.
Still need to do: Scene switching (To Main UI)

- P. Gruca:
Wrote tests, tried implementing final functionality for MainUI, problems with Edit instructions and Edit Ingredients. FoodItems redirection did workout (switching the scenes).

- K. Kadrieh:
Implemented Add/Edit/Delete functionality for FoodItems and actually made the all the connections with the database.
---
- Current State of the App (4 min) - Demonstration of what is on the main branch before the meeting

#### Minutes: 

- The newly implemented shopping list and it's functionality was shown. Other than that nothing could be show since the MR's for connecting the Main UI to the other UI's didn't 

---
- Current issues for discussion : (14 min)
    - Improved planning and communication (4 min)
        - How can we improve our planning and task division?
        - How can we improve communication?
    - Improvement of our GitLab use (8 min)
        - Review of the created issues and milestones
            - Does anyone have anything to add / change?
            - TA input: are our current issues up to standard, what improvements do we need to make?
        - Implementing the requirement of the requirement for 2 reviews on a merge request in GitLab - does everyone agree?
        - Assigning reviewers in GitLab - does everyone agree?
        - Usage of time estimates and time spent - does everyone agree?
        - Ideas from team members
            - Do we need more tags / issues / milestones, etc.
    - More flexible CheckStyle (2 min) - some UI-related methods are hard to get below 50 LOC
        - What can we do to ensure good code quality? - Change our CheckStyle rules / Suppress warnings / something else?
        - Team input: Ideas for improving quality
      
#### Minutes:

###### improved planning:
- MRs are hanging and requirements are unsure
- Maybe merge on Wednesday. Instead of one big merge, do more smaller merges to meet requirements
- Describe what your merge request is about in the description and redirect them from WhatsApp

###### Gitlab:

- Issues and milestones were made. However we must change them to be week based (the frequency must be at least one milestone per week and the milestones should describe what the shippable product should look like at the end of the week)
- Change requirement's for approval before merge requests to at least 2 people in gitlab setting.
- Assigning reviewers? Use it when you changed something in someone else's implementations and thus you want them to at least take a look at it.
- Approving agenda's does NOT count for your weekly requirement to approve one MR.
- Everyone should take a look at issues and edit the time estimates for the ones that were assigned to you. Issues should be about 3-4 hours.
- Use the completion status in GitLab for issues/milestones.. etc.

###### CheckStyle:

- Everyone gets a lot of "trivial" warnings
- Changed 50 lines per method max to 100 lines.
- About warning for fields/methods never used = suppress them in the FXML Ctrl class but keep them for other classes.

---
- Future Vision for the project (6 min)
    - What set of features should we implement next?
        - Which user epics and stories should we get started on?
        - Which features should we complete / improve?
    - Project management during the Midterm week
        - Should we maintain the agreed upon 24 h approval time for an MR?
        - Synchronization of our plans for the Midterm week and our code contribution planning for that week
    - Fine-grained task division to occur after the meeting - does everyone agree?

##### Minutes:

What do we want to achieve next:
- T. Corlateanu: Create nutritional calculations in Recipes.
- A. Iliev: Keep working on shopping list, Add recipes in one go to shopping list, improve UI
- V. Kleidona: Contact Max and support him with the things that still must be implemented.
- K. Kadrieh: Write tests for Food item and add the final bullet points for the implementation.
- M. Tinkelenberg: Unknown
- For everyone: Implement scaling?

Project Management in midterm:
- In midterm week no one is going to work and we will all focus on midterms
- Next monday no meeting
- Monday week 6 is the next meeting, T. Corlateanu is joining online.
---

- Summarize action points: (2 min)
- Meeting Review (3 min)
    - Question round : Does anyone have anything to add before the meeting closes? (1 min)
    - Feedback round : What went well and what can be improved next time? (1 min)
    - Planned meeting duration vs Actual duration? (1 min)
  
##### Minutes:

Q/A:
- Removing lines: don't add or subtract lines to your line requirements, so it's neutral
- Added lines: count everything except from comments and whitespace.
- Changed lines: TA is going to ask, respone after meeting.
- Minute-taker: Must pull the agenda, add the minutes there and then push it again, don't make a new file. Put the corresponding notes under each bullet-point
---

- Closure (1 min)