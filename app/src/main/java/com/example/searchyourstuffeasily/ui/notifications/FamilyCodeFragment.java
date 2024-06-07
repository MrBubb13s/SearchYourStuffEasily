package com.example.searchyourstuffeasily.ui.notifications;

import static android.app.PendingIntent.getActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
public class FamilyCodeFragment extends DialogFragment {
    private String familyCode;

    public void setFamilyCode(String familyCode) {
        this.familyCode = familyCode;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("가족 코드: " + familyCode + "\n\n가족 구성원으로 초대하고 싶은 사람에게\n코드를 전달해 주세요!")
                .setPositiveButton("확인", (dialog, id) -> {
                    // 필요한 추가 작업이 있다면 여기에 구현
                    copyFamilyCodeToClipboard(familyCode);
                });
        return builder.create();
    }

    private void copyFamilyCodeToClipboard(String familyCode) {
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("가족 코드", familyCode);
        clipboard.setPrimaryClip(clip);
    }
}