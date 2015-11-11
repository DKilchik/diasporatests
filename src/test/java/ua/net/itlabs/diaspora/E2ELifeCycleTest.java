package ua.net.itlabs.diaspora;

import steps.Relation;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import pages.*;
import ua.net.itlabs.BaseTest;

import static core.helpers.UniqueDataHelper.clearUniqueData;
import static core.helpers.UniqueDataHelper.the;
import static ua.net.itlabs.testDatas.Users.*;
import static pages.Aspects.WORK;
import static pages.Aspects.ACQUAINTANCES;

@Category(ua.net.itlabs.categories.Smoke.class)
public class E2ELifeCycleTest extends BaseTest {

    @BeforeClass
    public static void buildGivenForTests() {
        clearUniqueData();
    }

    @Test
    public void testUserActivitiesAndAccessForUsersOfOnePod() {
        //GIVEN - setup relation between users, addition one the same followed tag
        Relation.forUser(Pod1.rob).toUser(Pod1.ana, WORK).ensure();
        Relation.forUser(Pod1.ana).toUser(Pod1.rob, ACQUAINTANCES).doNotLogOut().ensure();

        //public post
        Menu.openStream();
        Feed.addPublicPost(the("Public Ana"));
        Feed.assertPost(Pod1.ana, the("Public Ana"));
        Menu.logOut();

        //like post, indirect check - public post is shown in stream of linked user
        Diaspora.signInAs(Pod1.rob);
        Feed.toggleLikePost(Pod1.ana, the("Public Ana"));
        Feed.assertLikes(Pod1.ana, the("Public Ana"), 1);

        //limited post in right aspect
        Feed.addAspectPost(WORK, the("Rob for work"));
        Feed.assertPost(Pod1.rob, the("Rob for work"));
        Menu.logOut();

        //comment post, indirect check - limited post in right aspect is shown in stream of linked user
        Diaspora.signInAs(Pod1.ana);
        Feed.addComment(Pod1.rob, the("Rob for work"), the("Comment from Ana"));
        Feed.assertComment(Pod1.rob, the("Rob for work"), Pod1.ana, the("Comment from Ana"));

        //check - for limited post is no possibility for resharing, indirect check - post is added
        Feed.assertPostCanNotBeReshared(Pod1.rob, the("Rob for work"));
        Menu.logOut();


        //check visibility of comments
        Diaspora.signInAs(Pod1.rob);
        Feed.assertComment(Pod1.rob, the("Rob for work"), Pod1.ana, the("Comment from Ana"));

        //reshare public post
        Feed.resharePost(Pod1.ana, the("Public Ana"));
        Feed.assertPost(Pod1.rob, the("Public Ana"));

        //unlike post
        Feed.toggleLikePost(Pod1.ana, the("Public Ana"));
        Feed.assertNoLikes(Pod1.ana, the("Public Ana"));
        Menu.logOut();

        //check visibility of reshared post
        Diaspora.signInAs(Pod1.ana);
        Feed.assertPost(Pod1.rob, the("Public Ana"));

        //delete post
        NavBar.openMyActivity();
        Feed.deletePost(Pod1.ana, the("Public Ana"));
        Feed.assertNoPost(Pod1.ana, the("Public Ana"));

        //check post of another user can not be deleted
        NavBar.openStream();
        Feed.assertPostCanNotBeDeleted(Pod1.rob, the("Rob for work"));

        //delete comment
        Feed.deleteComment(Pod1.rob, the("Rob for work"), Pod1.ana, the("Comment from Ana"));
        Menu.logOut();


        Diaspora.signInAs(Pod1.rob);
        //check - deleted post is not shown
        Feed.assertNoPost(Pod1.ana, the("Public Ana"));
        Menu.logOut();

    }

}