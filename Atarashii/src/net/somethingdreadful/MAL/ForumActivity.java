package net.somethingdreadful.MAL;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ViewFlipper;

import net.somethingdreadful.MAL.api.MALApi;
import net.somethingdreadful.MAL.api.response.ForumMain;
import net.somethingdreadful.MAL.dialog.ForumChildDialogFragment;
import net.somethingdreadful.MAL.dialog.MessageDialogFragment;
import net.somethingdreadful.MAL.forum.ForumsMain;
import net.somethingdreadful.MAL.forum.ForumsPosts;
import net.somethingdreadful.MAL.forum.ForumsTopics;
import net.somethingdreadful.MAL.tasks.ForumJob;
import net.somethingdreadful.MAL.tasks.ForumNetworkTask;
import net.somethingdreadful.MAL.tasks.ForumNetworkTaskFinishedListener;

public class ForumActivity extends ActionBarActivity implements MessageDialogFragment.onSendClickListener, ForumNetworkTaskFinishedListener, MessageDialogFragment.onCloseClickListener {

    public ForumsMain main;
    public ForumsTopics topics;
    public ForumsPosts posts;
    public boolean discussion = false;
    public ForumJob task = ForumJob.BOARD;
    public String message = "";
    FragmentManager manager;
    ViewFlipper viewFlipper;
    MenuItem search;
    Menu menu;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Theme.setTheme(this, R.layout.activity_forum, false);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        handleIntent(getIntent());

        viewFlipper = (ViewFlipper) findViewById(R.id.viewFlipper);
        manager = getFragmentManager();
        main = (ForumsMain) manager.findFragmentById(R.id.main);
        topics = (ForumsTopics) manager.findFragmentById(R.id.topics);
        posts = (ForumsPosts) manager.findFragmentById(R.id.posts);

        if (bundle != null) {
            viewFlipper.setDisplayedChild(bundle.getInt("child"));
            task = (ForumJob) bundle.getSerializable("task");
            discussion = bundle.getBoolean("discussion");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        state.putInt("child", viewFlipper.getDisplayedChild());
        state.putSerializable("task", task);
        state.putBoolean("discussion", discussion);
        super.onSaveInstanceState(state);
    }

    /**
     * Switch the view to the topics fragment.
     *
     * @param id The board id
     */
    public void getTopics(int id) {
        viewFlipper.setDisplayedChild(1);
        setTask(topics.setId(id, ForumJob.TOPICS));
    }

    /**
     * Switch the view to the topics fragment.
     *
     * @param query The query
     */
    public void getTopics(String query) {
        viewFlipper.setDisplayedChild(1);
        setTask(topics.setId(query));
    }

    /**
     * Switch the view to the topics fragment to show subBoards.
     *
     * @param id The subBoard id
     */
    public void getSubBoard(int id) {
        viewFlipper.setDisplayedChild(1);
        topics.type = ((id == 1 || id == 2) ? MALApi.ListType.ANIME : MALApi.ListType.MANGA);
        setTask(topics.setId(id, ForumJob.SUBBOARD));
    }

    /**
     * Switch the view to the topics posts.
     *
     * @param id The id of the topic
     */
    public void getPosts(int id) {
        viewFlipper.setDisplayedChild(2);
        setTask(posts.setId(id));
    }

    /**
     * Create the edithor dialog.
     *
     * @param id      The comment id
     * @param message The comment text
     * @param task    The task to peform
     */
    public void getComments(int id, String message, ForumJob task) {
        MessageDialogFragment info = new MessageDialogFragment().setListeners(this, this);
        Bundle args = new Bundle();
        args.putInt("id", id);
        args.putString("message", message);
        args.putSerializable("task", task);
        info.setArguments(args);
        info.show(getFragmentManager(), "fragment_forum");
    }

    /**
     * Switch the view to the discussion view.
     *
     * @param id The comment id
     */
    public void getDiscussion(int id) {
        viewFlipper.setDisplayedChild(1);
        setTask(topics.setId(id, ForumJob.DISCUSSION));
        discussion = true;
    }

    /**
     * Handle the back and home buttons.
     */
    public void back() {
        switch (task) {
            case BOARD:
                finish();
                break;
            case SUBBOARD:
                setTask(ForumJob.BOARD);
                viewFlipper.setDisplayedChild(0);
                break;
            case DISCUSSION:
                setTask(ForumJob.SUBBOARD);
                topics.task = ForumJob.SUBBOARD;
                topics.topicsAdapter.clear();
                topics.apply(topics.subBoard);
                discussion = false;
                break;
            case TOPICS:
            case SEARCH:
                setTask(ForumJob.BOARD);
                viewFlipper.setDisplayedChild(0);
                break;
            case POSTS:
                if (discussion) {
                    setTask(ForumJob.DISCUSSION);
                } else
                    setTask(ForumJob.TOPICS);
                viewFlipper.setDisplayedChild(1);
                break;
        }
        message = "";
    }

    /**
     * Refresh the displayed view.
     */
    public void refresh() {
        if (task == ForumJob.TOPICS)
            topics.getRecords(topics.page, topics.task);
        else
            posts.getRecords(posts.page);
    }

    /**
     * Change the task & change the menu items.
     *
     * @param task The new ForumTask
     */
    public void setTask(ForumJob task) {
        this.task = task;
        menu.findItem(R.id.action_add).setVisible((task == ForumJob.POSTS || task == ForumJob.TOPICS) && getTopicStatus() && viewFlipper.getDisplayedChild() != 3);
        menu.findItem(R.id.action_send).setVisible(viewFlipper.getDisplayedChild() == 3);
        menu.findItem(R.id.action_ViewMALPage).setVisible(viewFlipper.getDisplayedChild() != 3);
        search.setVisible(task == ForumJob.BOARD);
    }

    /**
     * Checks if the ID allows to add a topics
     *
     * @return boolean If true then the ID allows to add comments
     */
    public boolean getTopicStatus() {
        return task != ForumJob.TOPICS || (topics.id != 5 && topics.id != 14 && topics.id != 15 && topics.id != 17);
    }

    @Override
    public void onBackPressed() {
        back();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_forum, menu);
        this.menu = menu;

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        ComponentName cn = new ComponentName(this, ForumActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(cn));
        search = searchItem;

        setTask(task);
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    /**
     * Handle the intent for the searchView
     *
     * @param intent The intent given by android
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            getTopics(query);
            topics.task = ForumJob.SEARCH;
            search.collapseActionView();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                back();
                break;
            case R.id.action_ViewMALPage:
                startActivity(new Intent(Intent.ACTION_VIEW, getUri()));
                break;
            case R.id.action_add:
                if (task == ForumJob.POSTS)
                    getComments(posts.id, message, ForumJob.ADDCOMMENT);
                else if (task == ForumJob.TOPICS)
                    getComments(topics.id, null, ForumJob.ADDTOPIC);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get the Uri depending on the ForumTask.
     *
     * @return Uri The uri of the desired URL to launch
     */
    public Uri getUri() {
        switch (task) {
            case BOARD:
                return Uri.parse("http://myanimelist.net/forum/");
            case SUBBOARD:
                return Uri.parse("http://myanimelist.net/forum/?subboard=" + topics.id);
            case DISCUSSION:
                if (ForumChildDialogFragment.DBModificationRequest)
                    return Uri.parse("http://myanimelist.net/forum/?topicid=" + topics.id);
                else
                    return Uri.parse("http://myanimelist.net/forum/?" + (topics.type == MALApi.ListType.ANIME ? "anime" : "manga") + "id=" + topics.id);
            case TOPICS:
                return Uri.parse("http://myanimelist.net/forum/?board=" + topics.id);
            case POSTS:
                return Uri.parse("http://myanimelist.net/forum/?topicid=" + posts.id);
        }
        return null;
    }

    @Override
    public void onSendClicked(String message, String subject, ForumJob task, int id) {
        if (task == ForumJob.ADDTOPIC && !message.equals("") && !subject.equals(""))
            new ForumNetworkTask(this, this, task, id).execute(subject, message);
        else if (!message.equals(""))
            new ForumNetworkTask(this, this, task, id).execute(message);
    }

    @Override
    public void onForumNetworkTaskFinished(ForumMain result, ForumJob task) {
        if (task == ForumJob.ADDCOMMENT)
            Theme.Snackbar(this, R.string.toast_info_comment_add);
        if (task == ForumJob.ADDTOPIC || task == ForumJob.ADDCOMMENT || task == ForumJob.UPDATECOMMENT)
            Theme.Snackbar(this, R.string.toast_info_comment_added);
        refresh();
    }

    @Override
    public void onCloseClicked(String message) {
        this.message = message;
    }
}
