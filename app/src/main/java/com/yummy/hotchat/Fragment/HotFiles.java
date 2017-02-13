package com.yummy.hotchat.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yummy.hotchat.Adapter.HotFilesAdapter;
import com.yummy.hotchat.Global.ConstantC;
import com.yummy.hotchat.Global.ToastMaker;
import com.yummy.hotchat.Object.FileInfo;
import com.yummy.hotchat.Object.FileSortByTime;
import com.yummy.hotchat.R;
import com.yummy.hotchat.Widget.CreatingDialog;
import com.yummy.hotchat.Widget.FileDetailDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.yummy.hotchat.Global.MIMEType.getMIMEType;
import static com.yummy.hotchat.Object.FileInfo.switchTime;


public class HotFiles extends Fragment {

    private View view;
    private final static String TAG = "HotFiles";
    private List<FileInfo> fileList; //存储文件的信息
    private SwipeRefreshLayout srlFile;
    private HotFilesAdapter hotFilesAdapter;
    private RecyclerView rvFilesList; //显示文件的list
    private CreatingDialog firstRefreshDialog; //刷新文件时的进度窗口

    private FilesHandler filesHandler;

    private File filesDirectory; //文件的文件夹


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_hot_files, container, false);
        rvFilesList = (RecyclerView) view.findViewById(R.id.rv_files_list);
        srlFile = (SwipeRefreshLayout) view.findViewById(R.id.srl_files_list);
        initParameter();
        initFilesListAdapter();

        return view;
    }

    /**
     * 初始化Handler和文件保存的根目录
     */
    void initParameter() {

        filesHandler = new FilesHandler();
        filesDirectory = new File(Environment.getExternalStorageDirectory().getPath() + "/HotChatFiles/");
    }


    /**
     * 初始化文件列表相关
     */
    void initFilesListAdapter() {

        fileList = new ArrayList<>();
        hotFilesAdapter = new HotFilesAdapter(getActivity(), fileList);

        /**
         * 设置文件项目上打开和删除按钮的点击事件
         */
        hotFilesAdapter.setOnFileItemClickListener(new HotFilesAdapter.OnFileItemClickListener() {
            @Override
            public void onDeleteClick(final int position) {
                Log.i(TAG, "position:" + position + "delete clicked");
                final File file = new File(fileList.get(position).getFilePath());
                if (file.exists()) {
                    Log.i(TAG, "文件存在，删除文件");
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("确定要删除该文件吗？");
                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            file.delete();
                            fileList.remove(position);
                            hotFilesAdapter.notifyDataSetChanged();
                            ToastMaker.toastSomething(getActivity(), "文件已删除成功~");
                        }
                    });
                    builder.setMessage("删除的文件将不可恢复~");
                    builder.show();

                } else {
                    Log.i(TAG, "文件不存在，删除失败");
                    ToastMaker.toastSomething(getActivity(), "文件不存在，删除失败！");
                }

            }

            @Override
            public void onOpenClick(final int position) {
                Log.i(TAG, "position:" + position + "open clicked");
                final FileDetailDialog fileDetailDialog = new FileDetailDialog(getActivity(), getActivity().getLayoutInflater(), R.layout.dialog_file_details, fileList.get(position));
                fileDetailDialog.initParameterAndShow();
                fileDetailDialog.setOnOpenClickedListener(new FileDetailDialog.OnOpenClickedListener() {
                    @Override
                    public void OnOpenClick() {
                        Log.i(TAG, "调用系统程序打开文件" + position);
                        openFile(new File(fileList.get(position).getFilePath()));
                        fileDetailDialog.dismiss();
                    }

                    @Override
                    public void OnCancelClick() {
                        Log.i(TAG, "取消打开文件" + position);
                        fileDetailDialog.dismiss();
                    }
                });

            }

            @Override
            public void onFileItemLongClicked(final int position) {
                Log.i(TAG, "position:" + position + "long clicked");
                final FileDetailDialog fileDetailDialog = new FileDetailDialog(getActivity(), getActivity().getLayoutInflater(), R.layout.dialog_file_details, fileList.get(position));
                fileDetailDialog.initParameterAndShow();
                fileDetailDialog.setOnOpenClickedListener(new FileDetailDialog.OnOpenClickedListener() {
                    @Override
                    public void OnOpenClick() {
                        Log.i(TAG, "调用系统程序打开文件" + position);
                        openFile(new File(fileList.get(position).getFilePath()));
                        fileDetailDialog.dismiss();
                    }

                    @Override
                    public void OnCancelClick() {
                        Log.i(TAG, "取消打开文件" + position);
                        fileDetailDialog.dismiss();
                    }
                });
            }
        });

        /**
         * 初始化recyclerView
         */
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        rvFilesList.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        rvFilesList.setAdapter(hotFilesAdapter);
        rvFilesList.setItemAnimator(new DefaultItemAnimator());


        srlFile.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent);

        srlFile.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "FileList 刷新");

                new ScanFilesThread().start();

            }
        });

        firstRefreshDialog = new CreatingDialog(getActivity(), getActivity().getLayoutInflater(), R.layout.dialog_creating, "正在获取文件");
        firstRefreshDialog.initParameterAndShow();
        new ScanFilesThread().start();


    }


    /**
     * 定义本Fragment的handler
     */
    class FilesHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConstantC.FILE_SCAN_COMPLETED:

                    if (firstRefreshDialog.isShowing()) {
                        firstRefreshDialog.dismiss();
                    }
                    srlFile.setRefreshing(false);
                    Collections.sort(fileList, new FileSortByTime());
                    hotFilesAdapter.setData(fileList);
                    hotFilesAdapter.notifyDataSetChanged();

                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 扫描文件夹内文件的线程
     */
    class ScanFilesThread extends Thread {
        @Override
        public void run() {

            fileList.clear();
            getFileList(filesDirectory, fileList);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Message msg = new Message();
            msg.what = ConstantC.FILE_SCAN_COMPLETED;
            filesHandler.sendMessage(msg);

        }
    }

    /**
     * @param path
     * @param fileList 注意的是并不是所有的文件夹都可以进行读取的，权限问题
     */
    private void getFileList(File path, List<FileInfo> fileList) {
        //如果是文件夹的话
        if (path.isDirectory()) {
            //返回文件夹中有的数据
            File[] files = path.listFiles();
            //先判断下有没有权限，如果没有权限的话，就不执行了
            if (null == files)
                return;

            for (int i = 0; i < files.length; i++) {
                getFileList(files[i], fileList);
            }
        }
        //如果是文件的话直接加入
        else {
//            Log.i(TAG, "文件路径：" + path.getAbsolutePath());
//            Log.i(TAG, "文件名：" + path.getName() + "，文件大小：" + path.length() / 1024 + "KB，文件时间：" + switchTime(path.lastModified()));
            //进行文件的处理
            String filePath = path.getAbsolutePath();
            //文件名
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            //添加
            FileInfo fileInfo = new FileInfo(path.getName(), path.length() / 1024 + "KB", path.getAbsolutePath(), switchTime(path.lastModified()));
            fileInfo.setLastModified(path.lastModified());
//            fileList.put(fileName, filePath);
            Log.i(TAG, fileInfo.toString());
            fileList.add(fileInfo);
        }
    }

    /**
     * 打开文件
     *
     * @param file
     */
    private void openFile(File file) {

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        //获取文件file的MIME类型
        String type = getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
        //跳转
        startActivity(intent);

    }

}

