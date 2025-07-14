package com.ssafy.core.service;

import com.ssafy.core.utils.MessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

    @Value("${nurigo.api.sender}")
    private String sender;

    private final MessageUtil messageUtil;

    /**
     * 단일 메시지 발송
     */
    public CommonResult sendOne(String to, String text) throws IOException {
        CommonResult result = new CommonResult();
        try{
            String targetUrl = "http://api.solapi.com/messages/v4/send";
            String from = sender.replaceAll("-","");
            String parameters = "{" +
                    "\"message\":" +
                    "{\"to\":\"" + to + "\"" +
                    ",\"from\":\"" + from + "\"" +
                    ",\"text\":\"" + text + "\"}" +
                    "}";

            String authorization = messageUtil.getHeaders();

            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", authorization);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(parameters.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            result.setOutput(responseCode);
            result.setMsg(con.getResponseMessage());

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;

            StringBuffer response = new StringBuffer();

            while ((line = in.readLine()) != null) {
                response.append(line);
            }

            in.close();

            log.info("HTTP response code : " + responseCode);
            log.info("HTTP body : " + response.toString());

        }catch (Exception e){
            e.printStackTrace();
        }

        return result;
    }
}
