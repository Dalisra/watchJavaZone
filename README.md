This project is a video discovery platform for JavaZone talks — essentially a "Watch JavaZone" site, similar to YouTube but focused exclusively on conference talks from JavaZone (a Norwegian Java conference organized by javaBin).

The key goals are:

Make videos accessible — The talks are currently on Vimeo and hard to find. This site indexes them from the sleepingpill API and makes them browsable and searchable.
Search — Full-text search across talk titles, abstracts, speaker names, and keywords using PostgreSQL FTS.
Recommendations — Surface relevant videos based on what you watched, popular topics, and content similarity (overlapping keywords/speakers).
Analytics — Track what users watch, how far they get through a video, what they search for — feeding back into the recommendation engine.
The target audience is primarily the Norwegian developer community, and the platform would be analogous to something like watch.javazone.no.