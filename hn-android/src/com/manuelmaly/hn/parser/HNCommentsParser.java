package com.manuelmaly.hn.parser;

import com.manuelmaly.hn.App;
import com.manuelmaly.hn.Settings;
import com.manuelmaly.hn.model.HNComment;
import com.manuelmaly.hn.model.HNPostComments;
import com.manuelmaly.hn.util.HNHelper;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class HNCommentsParser extends BaseHTMLParser<HNPostComments> {

    @Override
    public HNPostComments parseDocument(Element doc) throws Exception {
        if (doc == null)
            return new HNPostComments();

        ArrayList<HNComment> comments = new ArrayList<HNComment>();

        Elements tableRows = doc.select("table tr table tr:has(table)");

        String currentUser = Settings.getUserName(App.getInstance());

        String text = null;
        String author = null;
        int level = 0;
        String timeAgo = null;
        String url = null;
        Boolean isDownvoted = false;
        String upvoteUrl = null;
        String downvoteUrl = null;

        boolean endParsing = false;
        for (int row = 0; row < tableRows.size(); row++) {
            Element mainRowElement = tableRows.get(row).select("td:eq(2)").first();
            Element rowLevelElement = tableRows.get(row).select("td:eq(0)").first();
            if (mainRowElement == null)
                continue;

            // The not portion of this query is meant to remove the reply link
            // from the text.  As far as I can tell that is the only place
            // where size=1 is used.  If that turns out to not be the case then
            // searching for u tags is also a pretty decent option - @jmaltz
            Element mainCommentSpan = mainRowElement.select("span.comment > span").first();
            if (mainCommentSpan == null)
                continue;

            mainCommentSpan.select("div.reply").remove();

            // In order to eliminate whitespace at the end of multi-line comments,
            // <p> tags are replaced with double <br/> tags.
            text = mainCommentSpan.html()
                     .replace("<span> </span>", "")
                     .replace("<p>", "<br/><br/>")
                     .replace("</p>", "");

            Element comHeadElement = mainRowElement.select("span.comhead").first();
            author = comHeadElement.select("a[href*=user]").text();
            timeAgo = comHeadElement.select("a[href*=item").text();//getFirstTextValueInElementChildren(comHeadElement);
//            if (timeAgoRaw.length() > 0)
//                timeAgo = timeAgoRaw.substring(0, timeAgoRaw.indexOf("|"));
            Element urlElement = comHeadElement.select("a[href*=item]").first();
            if (urlElement != null)
                url = urlElement.attr("href");

            String levelSpacerWidth = rowLevelElement.select("img").first().attr("width");
            if (levelSpacerWidth != null)
                level = Integer.parseInt(levelSpacerWidth) / 40;

            Elements voteElements = tableRows.get(row).select("td:eq(1) a");
            upvoteUrl = getVoteUrl(voteElements.first());

            // We want to test for size because unlike first() calling .get(1)
            // Will throw an error if there are not two elements
            if (voteElements.size() > 1)
               downvoteUrl = getVoteUrl(voteElements.get(1));

            comments.add(new HNComment(timeAgo, author, url, text, level, isDownvoted, upvoteUrl, downvoteUrl));

            if (endParsing)
                break;
        }

        // Just using table:eq(0) would return an extra table, so we use
        // get(0) instead, which only returns only the one we want
        Element header = doc.select("body table:eq(0)  tbody > tr:eq(2) > td:eq(0) > table").get(0);
        String headerHtml = null;

        // Five table rows is what it takes for the title, post information
        // And other boilerplate stuff.  More than five means we have something
        // Special
        if(header.select("tr").size() > 5) {
            HeaderParser headerParser = new HeaderParser();
            headerHtml = headerParser.parseDocument(header);
        }

        return new HNPostComments(comments, headerHtml, currentUser);
    }

    /**
     * Parses out the url for voting from a given element
     * @param voteElement The element from which to parse out the voting url
     * @return The relative url to vote in the given direction for that comment
     */
    private String getVoteUrl(Element voteElement) {
        if (voteElement != null) {
            return voteElement.attr("href").contains("auth=") ?
                HNHelper.resolveRelativeHNURL(voteElement.attr("href")) : null;
        }

        return null;
    }

}
