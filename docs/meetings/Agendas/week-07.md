| Key | Value                                                            |
| --- |------------------------------------------------------------------|
| Date : | 9-01-2026                                                        |
| Time : | 14:45 - 15:30                                                    |
| Location : | DW Hall 2                                                        |
| Chair | K. Kadrieh                                                       |
| Minute Taker | T. Corlateanu                                                    |
| Attendees : | K. Kadrieh, A. Iliev, V. Kleidona, T. Corlateanu, E. Tskhay (TA) |

### Agenda Items :

#### - General Topics:

- Opening by chair (1 min)
- Check-in: How is everyone doing? How were the holidays? (2 min)
- Announcements by the team (1 min)
- Approval of the agenda - Does anyone have any additions? (1 min)
- Approval of last minutes - Did everyone read the last minutes? Do you approve of these? (1 min)
- Announcements by the TA (5 min)
- Individual progress update (2 min each, 8 min total)
- Presentation of the current app to TA (5 min)

#### - Talking Points: (Inform/ brainstorm/ decision-making/ discuss)

- < Agenda - Grade related topics > (9 min )
  - Milestones, issues and time tracking: (4 mins)
    - Is everyone using milestones in their MR's? Who should and when should we make milestones?
    - Is everyone making and using issues? Is everyone adding clear descriptions
    - Is everyone make use of the time tracking features?
  - Discussions and commits in MR's: (3 mins)
    - Question for the TA: Recommended amount of commits per MR, Document says 'few'? Is everyone averaging about 10 commits per MR?
    - Discussions and reviews in GitLab: Do we want to improve them, longer discussions, more constructive reviews?
  - Question for the TA: Do aesthetic and appearance of the app matter, or is the grading just about if the features work correctly? (2 min)
- < Agenda - Nearing the deadline > (4 min)
  - When do we plan on finishing the new features we started working on? There is still one feature left, when do we start and finish it? (2 min)
  - Do we want to add new features in the last week or do we instead want to optimize things and wrap up? (2 min)
- < Agenda - Team related topics > (3 min):
  - Are we doing well with 2 people down? Do we need to speed up/slow down or is our tempo good?
  - Are the tasks of the missing people taken over and finished? (Is the main menu still good or does it need improvement?)

#### - Wrapping up:
- Summarize action points: Who, what, when? (3 min)
- Feedback round: What went well and what can be improved next time ? (1 min )
- Planned meeting duration != actual duration? Where/why did you misestimate ? (1 min )
- Question round: Does anyone have anything to add before the meeting closes ? (2 min)
  - Decide for a chair and minute-taker next week.
- Closure (1 min )


Minutes - week 7

### Agenda Items :

#### - General Topics:

- Opening by chair (1 min, actual 1 min)

- Check-in: How is everyone doing? How were the holidays? (2 min, actual 1min)
  Everybody is doing fine.
- Announcements by the team (1 min, actual 1min)
  No announcements.
- Approval of the agenda - Does anyone have any additions? (1 min, actual 1min)
  Agenda is approved, nobody had any additions to make.
- Approval of last minutes - Did everyone read the last minutes? Do you approve of these? (1 min, actual 1)
  Everyone read the last minutes and approved those.
- Announcements by the TA (5 min, actual 5min)
  There will be a compensation for missing members, a little grade bump for the rest of the team members (not very significant).
  Technology feedback became available on brightspace.
  The implemented features feedback will become available soon.
  Max has been removed, probably the same will happen with Pawel.
- Individual progress update (2 min each, 8 min total, actual 8min)

Teodora:
worked on implementing the first five bullet points of 4.4, namely: the user can favorite/unfavorite a recipe,
the user can see 2 tabs(all recipes/favorites)
added tests for the MainCtrl class

Vasiliki:
implemented the remaining bullet point for basic requirements: 'clone a recipe' feature
finished the EditIngredientsUI, adding the dropdown menu for the units, along with the system change, like in the createRecipe UI

Aleksander:
worked on 4.2: Automated Change Synchronization, which he almost finished and will make a MR soon.

Khaled:
worked on 4.4, implementing a search bar, that can identify recipes containing the searched word within the list of
ingredients, instructions or name.
more complex searches are also available, using a comma between words.
the user can search in the 2 different tabs, so if they are in 'favorites tab', they search result will
contain only favorite recipes and the same for 'all recipes tab'.

- Presentation of the current app to TA (5 min, actual 3 min)
  new: favorite tabs, clone recipe, search for recipes, automated synchronization to be merged, updated EditIngredients UI


#### - Talking Points: (Inform/ brainstorm/ decision-making/ discuss)

- < Agenda - Grade related topics > (9 min, actual 7min)
  - Milestones, issues and time tracking: (4 mins, actual 4min)
    - Is everyone using milestones in their MR's? Who should and when should we make milestones?
      Everyone is using milestones, after discussing for a bit, everybody came to the conclusion that the best way to handle the creation of
      milestones is creating them on Friday, after the end of the meeting.

    - Is everyone making and using issues? Is everyone adding clear descriptions
      Everyone is using issues, Teodora and Vasiliki will improve the description.
      The TA suggested to have maximum 1 bullet point(user story) per issue, however, if in 3 to 4 hours we cover more than 1 user story, we can
      have an issue containing multiple bullet points, as long as the description is clear
      (the TA will let us know on mattermost, to be 100% sure this is the case).

    - Is everyone make use of the time tracking features?
      Vasiliki still needs to edit some MRs, everybody else uses them.

  - Discussions and commits in MR's: (3 mins, actual 3min)
    - Question for the TA: Recommended amount of commits per MR, Document says 'few'? Is everyone averaging about 10 commits per MR?
      On average, 10 commits per MR.

    - Discussions and reviews in GitLab: Do we want to improve them, longer discussions, more constructive reviews?
      A concrete discussion is needed to get an excellent, this discussion can be regarding: what the code does, any changes that should
      be made, asking the other person to clarify parts of code that are not clear enough etc. The discussion should be constructive and help
      improve the final product.

  - Question for the TA: Do aesthetic and appearance of the app matter, or is the grading just about if the features work correctly? (2 min, 2min)
    The way the app looks does not really matter, the functionality is much more important, however, it must be taken
    into account.

- < Agenda - Nearing the deadline > (4 min, actual 2min)
  - When do we plan on finishing the new features we started working on? There is still one feature left, when do we start and finish it? (2 min)
    Deadline 23 jan 23:59.
    Teodora suggested to work this week and the next one, then start fixing. However, the others were concerned whether there will be enough time
    to fix everything or not. The conclusion that was made was: 2 people start working on fixing next week, while the other 2 work on implementing the
    remaining features, and, in the final week, everybody works on fixing what is left.

  - Do we want to add new features in the last week or do we instead want to optimize things and wrap up? (2 min, 1min)
    In the last week, our only concern will be to optimize things and wrap everything up.

- < Agenda - Team related topics > (3 min, actual 5min)):
  - Are we doing well with 2 people down? Do we need to speed up/slow down or is our tempo good?
    Given the fact that 2 people are missing, we are doing well, our tempo is on point, and
    we need to maintain it over the next weeks.

  - Are the tasks of the missing people taken over and finished? (Is the main menu still good or does it need improvement?)
    Vasiliki took over Max's tasks, she implemented everything.
    Pawel's UI still needs some improvement, Teodora said she will work on that.

#### - Wrapping up:
- Summarize action points: Who, what, when? (3 min, actual 3min)
  After this meeting, we set the milestones for the next week.
  Aleksandar will finish 4.2 .
  Khaled will work on changing the UIs layout and adding the application logo, some 4.4 bullet points and tests.
  Vasiliki will try to implement the Language Switch requirement and add some tests.
  Teodora will keep working on the 4.4 bullet points.
  Everybody will try to work on their issues.


- Feedback round: What went well and what can be improved next time ? (1 min, actual 1min)
  Everybody agreed that the meeting went well.

- Planned meeting duration != actual duration? Where/why did you misestimate ? (1 min, actual 1min)
  Everything was discussed, we miss 2 people so that is why it took 5 minutes less than intended.
  planned duration: 45min
  actual duration:  40min

- Question round: Does anyone have anything to add before the meeting closes ? (2 min, actual 2min)
  The 'Automated Change Synchronization' must be stable and functional, nothing too complex is expected.
  There is no need for adding new filters for the search bar (such as searching an ingredient, a name or an instruction), as the
  backlog does not mention it.

  - Decide for a chair and minute-taker next week.
    Teodora: Chair.
    Vasiliki: Minute-taker.

- Closure (1 min, actual 1min)
  The TA will let us know the situation on issues.

