package com.springboot.boot;

import com.springboot.service.UserService;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

@RestController
@RequestMapping("/users1")
public class ExploreXMLAction {
    private LinkedBlockingQueue<Object> queue = new LinkedBlockingQueue<Object>(30000);

    @RequestMapping(value = "/{id}",method = RequestMethod.GET)
    public void save(@PathVariable String id) {
        Date begin =  new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(begin);
        System.out.println("begin---------" + dateString);
        Producer read = new Producer();
        Consumer consumerread = new Consumer();
        Producer moniotrTask=   SpringUtils.getBean("mProducer", Producer.class);
        Consumer consumermoniotrTask=   SpringUtils.getBean("mConsumer", Consumer.class);
        for(int i=0;i<7;i++) {
            new Thread(consumermoniotrTask, (i + 1) + "").start();
        }
        for(int i=0;i<3;i++) {
            new Thread(moniotrTask, "test" + ((i + 1) + "")).start();
        }


    }

    @Component("mProducer")
    @Scope("prototype")
    class Producer implements Runnable {

//        @Autowired
//        private UserService userService;

        List<File> filePathsList = new ArrayList<File>();
        int index = 0;

        public Producer() {
            File f = new File("d:" + File.separator + "files");
            getFileList(f);
        }

        private void getFileList(File f) {
            File[] filePaths = f.listFiles();
            for (File s : filePaths) {
                if (s.isDirectory()) {
                    getFileList(s);
                } else {
                    if (-1 != s.getName().lastIndexOf(".xml")) {
                        filePathsList.add(s);
                    }
                }
            }
        }

        @Override
        public void run() {
            File file = null;
            while (index < filePathsList.size()) {
                synchronized (this) {
                    if (index >= filePathsList.size()) {
                        continue;
                    }
                    file = filePathsList.get(index);
                    index++;
                }
                // xml
                SAXBuilder builder = new SAXBuilder();
                List<String> xmlList = new ArrayList<String>();
                try {
                    InputStream is = new FileInputStream(file.getPath());
//                    System.out.println("currentThread："
//                            + Thread.currentThread().getName() + ",is reading:"
//                            + filePathsList.indexOf(file) + ",currnetListLength:"
//                            + filePathsList.size());
                    Document doc = builder.build(is);
                    Element root = doc.getRootElement();
                    List<Element> list = root.getChildren();
                    for (Element e : list) {
                        xmlList.add(e.getChildTextTrim("ERROR_FEEDBACK_ID"));
                    }
                    Object o = new Object();
                    // 取出一个对象
                    queue.put(o);
                    xmlList.add("--------------------------");
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
//            long end = System.currentTimeMillis();
//            System.out.println("Total Time: " + (end - begin) + " ms");
        }
    }

    @Component("mConsumer")
    @Scope("prototype")
    class Consumer implements Runnable {

        @Autowired
        private UserService userService;

//        List<File> filePathsList = new ArrayList<File>();
//        int index = 0;
//        long begin = System.currentTimeMillis();
        public Consumer() {
//            File f = new File("d:" + File.separator + "files");
//            getFileList(f);
        }
//
//        private void getFileList(File f) {
//            File[] filePaths = f.listFiles();
//            for (File s : filePaths) {
//                if (s.isDirectory()) {
//                    getFileList(s);
//                } else {
//                    if (-1 != s.getName().lastIndexOf(".xml")) {
//                        filePathsList.add(s);
//                    }
//                }
//            }
//        }

        @Override
        public void run() {
            File file = null;
//            while (index < filePathsList.size()) {
            while (true) {
//                synchronized (this) {
//                    if (index >= filePathsList.size()) {
//                        continue;
//                    }
//                    file = filePathsList.get(index);
//                    index++;
//                }
                // xml
//                SAXBuilder builder = new SAXBuilder();
//                List<String> xmlList = new ArrayList<String>();
                try {
//                    InputStream is = new FileInputStream(file.getPath());
//                    System.out.println("mConsumercurrentThread："
//                            + Thread.currentThread().getName() + ",is reading:"
//                            + filePathsList.indexOf(file) + ",currnetListLength:"
//                            + filePathsList.size());
//                    Document doc = builder.build(is);
//                    Element root = doc.getRootElement();
//                    List<Element> list = root.getChildren();
//                    for (Element e : list) {
//                        xmlList.add(e.getChildTextTrim("ERROR_FEEDBACK_ID"));
//                    }
                    Object o = queue.take();
                    userService.save(Thread.currentThread().getName());
//                    xmlList.add("--------------------------");
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                long end = System.nanoTime();
//                System.out.println("Total Time: " + (end - begin) + " ms");
            }
        }
    }
}
