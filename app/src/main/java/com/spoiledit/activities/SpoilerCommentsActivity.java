package com.spoiledit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.makeramen.roundedimageview.RoundedImageView;
import com.spoiledit.R;
import com.spoiledit.adapters.SpoilerCommentsAdapter;
import com.spoiledit.constants.App;
import com.spoiledit.constants.Constants;
import com.spoiledit.constants.File;
import com.spoiledit.constants.Status;
import com.spoiledit.manager.FileManager;
import com.spoiledit.models.CommentModel;
import com.spoiledit.models.MovieSpoilerModel;
import com.spoiledit.utils.StringUtils;
import com.spoiledit.utils.ViewUtils;
import com.spoiledit.viewmodels.CommentsViewModel;
import com.squareup.picasso.Picasso;

public class SpoilerCommentsActivity extends RootActivity {
    public static final String TAG = SpoilerCommentsActivity.class.getCanonicalName();

    private CommentsViewModel commentsViewModel;

    private SwipeRefreshLayout swipeRefreshLayout;
    private EditText etComment;
    private SpoilerCommentsAdapter commentsAdapter;
    private FileManager fileManager;

    private boolean reply = false;
    private int commentId = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        commentsViewModel = ViewModelProviders.of(this).get(CommentsViewModel.class);
        setContentView(R.layout.activity_spoiler_comments);
    }

    @Override
    public void setUpToolBar() {
        setupBackIconOnly();
    }

    @Override
    public void initUi() {
        hideLoader();
        fileManager = new FileManager(this, File.From.SPOILERS);

        swipeRefreshLayout = findViewById(R.id.srl_comments);
        etComment = findViewById(R.id.et_message);
    }

    @Override
    public void initialiseListener() {
        findViewById(R.id.iv_add_files).setOnClickListener(this);
        findViewById(R.id.iv_send).setOnClickListener(this);
        findViewById(R.id.tv_comment).setOnClickListener(this);
        findViewById(R.id.tv_thumbs_up).setOnClickListener(this);
        findViewById(R.id.tv_thumbs_down).setOnClickListener(this);
    }

    @Override
    public void setData() {
        MovieSpoilerModel spoilerModel = commentsViewModel.getMovieSpoilerModel();

        StringUtils.setText(findViewById(R.id.tv_username), spoilerModel.getDisplayName());
        StringUtils.setText(findViewById(R.id.tv_date), spoilerModel.getCratedOn());
        StringUtils.setText(findViewById(R.id.tv_spoiler), spoilerModel.getSpoiler());
        StringUtils.setText(findViewById(R.id.tv_thumbs_up), "(" + spoilerModel.getThumbsUp() + ")");
        StringUtils.setText(findViewById(R.id.tv_thumbs_down), "(" + spoilerModel.getThumbsDown() + ")");

        Picasso.get()
                .load(spoilerModel.getAvatarUrl())
                .resize(80, 80)
                .centerCrop().error(getResources().getDrawable(R.drawable.popcorn))
                .into((RoundedImageView) findViewById(R.id.riv_user));
    }

    @Override
    public void setUpRecycler() {
        RecyclerView recyclerView = findViewById(R.id.rv_comments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        commentsAdapter = new SpoilerCommentsAdapter(this, new SpoilerCommentsAdapter.OnCommentActionListener() {
            @Override
            public void onReplyComment(CommentModel commentModel) {
                reply = true;
                commentId = commentModel.getId();
            }

            @Override
            public void onLikeComment(CommentModel commentModel) {

            }

            @Override
            public void onDislikeComment(CommentModel commentModel) {

            }
        });

        recyclerView.setAdapter(commentsAdapter);
        ViewUtils.addFabOffset(this, recyclerView);

        commentsAdapter.registerAdapterDataObserver(getAdapterDataObserver());
    }

    @Override
    public void addObservers() {
        commentsViewModel.getApiStatusModelMutable().observe(this, apiStatusModel -> {
            if (apiStatusModel.getApi() == Constants.Api.MOVIE_COMMENTS) {
                if (apiStatusModel.getStatus() == Status.Request.API_HIT) {
                    showLoader(apiStatusModel.getMessage());

                } else {
                    hideLoader();
                    if (apiStatusModel.getStatus() == Status.Request.API_ERROR)
                        showFailure(false, apiStatusModel.getMessage());
                }
            } else if (apiStatusModel.getApi() == Constants.Api.MOVIE_COMMENT_ADD
                    || apiStatusModel.getApi() == Constants.Api.MOVIE_COMMENT_EDIT
                    || apiStatusModel.getApi() == Constants.Api.MOVIE_COMMENT_REPLY
                    || apiStatusModel.getApi() == Constants.Api.MOVIE_COMMENT_DELETE) {
                if (apiStatusModel.getStatus() == Status.Request.API_SUCCESS)
                    requestData();
            }
        });

        commentsViewModel.getCommentsModelMutable().observe(this, commentModels -> {
            if (commentModels != null)
                commentsAdapter.setItems(commentModels);
        });
    }

    @Override
    public void requestData() {
        showLoader();
        commentsViewModel.requestComments();
    }

    @Override
    public void onRefresh() {
        commentsViewModel.requestComments();
    }

    @Override
    public void onAdapterDataChanged() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.iv_add_files)
            fileManager.showFileSources();
        else if (v.getId() == R.id.tv_comment) {
            reply = false;
            commentId = 0;
            etComment.setText("");
        } else if (v.getId() == R.id.iv_send) {
            if (!StringUtils.isInvalid(etComment)) {
                if (reply)
                    commentsViewModel.replyComment(commentId, etComment.getText().toString().trim());
                else
                    commentsViewModel.addComment(etComment.getText().toString().trim());

                reply = false;
                commentId = 0;
                etComment.setText("");
            }
        } else
            super.onClick(v);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == App.Usage.BROWSER) {
            fileManager.gotoFileBrowser();
        } else if (requestCode == App.Usage.GALLERY) {
            fileManager.gotoGallery();
        } else if (requestCode == App.Usage.CAMERA) {
            fileManager.gotoCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == App.SystemIntent.GALLERY) {
            if (resultCode == RESULT_OK) {
//                updateProfilePic(fileManager.getFileDataFromIntent(data));
            }
        } else if (requestCode == App.SystemIntent.BROWSER) {
            if (resultCode == RESULT_OK) {
//                updateProfilePic(fileManager.getFileDataFromIntent(data));
            }
        } else if (requestCode == App.SystemIntent.CAMERA) {
            if (resultCode == RESULT_OK) {
//                updateProfilePic(fileManager.getImagePathForCamera());
            }
        }
    }
}
