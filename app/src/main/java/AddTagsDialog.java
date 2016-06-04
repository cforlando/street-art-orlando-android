package com.cforlando.streetartandroid;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import me.kaede.tagview.Tag;
import me.kaede.tagview.TagView;

/**
 * Created by benba on 4/20/2016.
 */
public class AddTagsDialog extends DialogFragment {

    private EditText mEditText;
    private TagView mTagView;

    public interface AddTagsDialogListener {
        public void onReturnTags(List<Tag> tags);
    }

    AddTagsDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (AddTagsDialogListener) activity;
        } catch (ClassCastException e) {
            //Activity doesn't implement the interface
            throw new ClassCastException(activity.toString()
            + " must implement AddTagsDialogListener");
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_tags, null);
        builder.setView(dialogView);

        final TagView tagView = (TagView) dialogView.findViewById(R.id.tagview);
        final EditText editText = (EditText) dialogView.findViewById(R.id.edit_tag);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (!editText.getText().toString().equals("")) {
                        Tag tag = new Tag(editText.getText().toString());
                        tag.isDeletable = true;
                        tagView.addTag(tag);
                        editText.setText("");
                    }
                    handled = true;
                }
                return handled;
            }
        });


        builder.setMessage("Add Tags")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AddTagsDialogListener activity = (AddTagsDialogListener) getActivity();
                        String text = editText.getText().toString();
                        Tag tag = new Tag(text);
                        tag.isDeletable = true;
                        tagView.addTag(tag);
                        activity.onReturnTags(tagView.getTags());
                    }
                });

        return builder.create();
    }
}
