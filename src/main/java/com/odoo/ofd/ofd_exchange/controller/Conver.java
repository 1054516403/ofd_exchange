package com.odoo.ofd.ofd_exchange.controller;


import com.odoo.ofd.ofd_exchange.Util;
import org.apache.pdfbox.io.IOUtils;
import org.ofdrw.converter.export.ImageExporter;
import org.ofdrw.converter.export.OFDExporter;
import org.ofdrw.converter.export.PDFExporterIText;
import org.ofdrw.converter.export.PDFExporterPDFBox;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class Conver {

    @PostMapping("/ofd2png")
    public HashMap<String, Object> upload(
            @RequestPart MultipartFile file,
            @RequestParam String fp_no
            ) {
        HashMap<String, Object> h = new HashMap<>();
        String filePath = "upload/" + fp_no + ".ofd";
        // 先保存上传的ofd文件
        try (InputStream is = file.getInputStream()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            IOUtils.copy(is, os);
            byte[] bytes = os.toByteArray();
            File uploadFile = new File(filePath);
            if ( uploadFile.exists()) {
                uploadFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(uploadFile);
            fos.write(bytes);
            fos.close();
            os.close();
        }catch (IOException e) {
            h.put("status", 400);
            h.put("msg", "File upload failure");
            return h;
        }
        Util u = new Util();
        Path ofdPath = Paths.get(filePath);
        Path outPath = Paths.get("./converter/" + fp_no + ".png");
        try (ImageExporter exporter = new ImageExporter(ofdPath, outPath, "PNG", 20d)) {
            exporter.export();
            List<Path> imgFilePaths = exporter.getImgFilePaths();
            System.out.println(imgFilePaths.get(0).toAbsolutePath());
            List<String> r = new ArrayList<String>();
            for (int i = 0; i < imgFilePaths.size(); i++) {
                r.add("data:image/png;base64," + u.encodeBase64FromFilePath(imgFilePaths.get(i).toAbsolutePath().toString()));
                imgFilePaths.get(i).toFile().delete();
            }

            h.put("data", r);
            h.put("status", 200);
            return h;
        } catch (IOException e) {
            h.put("status", 500);
            h.put("msg", "File conversion failure");
            return h;
        } finally {
            u.deleteDir(new File("./converter/" + fp_no + ".png"));
            ofdPath.toFile().delete();
        }
    }

    @PostMapping("/ofd2pdf")
    public HashMap<String, Object> ofd2Pdf(
            @RequestPart MultipartFile file,
            @RequestParam String fp_no
    ) {
        HashMap<String, Object> r = new HashMap<>();
        String filePath = "upload/" + fp_no + ".ofd";
        // 先保存上传的ofd文件
        try (InputStream is = file.getInputStream()) {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            IOUtils.copy(is, os);
            byte[] bytes = os.toByteArray();
            File uploadFile = new File(filePath);
            if ( uploadFile.exists()) {
                uploadFile.delete();
            }
            FileOutputStream fos = new FileOutputStream(uploadFile);
            fos.write(bytes);
            fos.close();
            os.close();
        }catch (IOException e) {
            r.put("status", 400);
            r.put("msg", "File upload failure");
            return r;
        }
        Path ofdPath = Paths.get(filePath);
        Path pdfPath = Paths.get("./converter/" + fp_no + ".pdf");
        try (OFDExporter exporter = new PDFExporterIText(ofdPath, pdfPath)) {
            exporter.export();
        } catch (IOException e) {
            r.put("status", 500);
            r.put("msg", "File conversion failure");
            return r;
        }
        Util u = new Util();
        try {
            String s = u.encodeBase64FromFilePath(pdfPath.toAbsolutePath().toString());
            r.put("status", 200);
            r.put("data", s);
            return r;
        } catch (IOException e) {
            r.put("status", 500);
            r.put("msg", "File encoding failure");
            throw new RuntimeException(e);
        } finally {
            pdfPath.toFile().delete();
            ofdPath.toFile().delete();
        }
    }
}
