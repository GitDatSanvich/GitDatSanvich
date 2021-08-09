package com.gitdatsanvich.sweethome.service.impl;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlDivision;
import com.gargoylesoftware.htmlunit.html.HtmlHeading3;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableBody;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableHeader;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gitdatsanvich.common.constants.CommonConstants;
import com.gitdatsanvich.common.exception.BizException;
import com.gitdatsanvich.common.util.BlockedThreadPool;
import com.gitdatsanvich.sweethome.service.LawService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

/**
 * @author TangChen
 * @date 2021/7/5 16:16
 */
@Service
public class LawServiceImpl implements LawService {
    @Resource
    private BlockedThreadPool<List<Map<String, String>>> blockedThreadPool;

    private static final Map<Integer, String> EXCEL_HEADER = new HashMap<>();

    static {
        EXCEL_HEADER.put(1, "搜索字段");
        EXCEL_HEADER.put(2, "企业名称");
        EXCEL_HEADER.put(3, "统一社会信用代码");
        EXCEL_HEADER.put(4, "法定代表人");
        EXCEL_HEADER.put(5, "注册资本");
        EXCEL_HEADER.put(6, "实缴资本");
        EXCEL_HEADER.put(7, "所属行业");
        EXCEL_HEADER.put(8, "注册地址");
        EXCEL_HEADER.put(9, "经营范围");
        EXCEL_HEADER.put(10, "联系方式");
        EXCEL_HEADER.put(11, "来源网络地址");
    }

    @Override
    public List<Map<String, String>> getCompanyInfo(String companyNameSearch) throws IOException, BizException, InterruptedException {
        List<Map<String, String>> returnList = new ArrayList<>();
        Map<String, String> basicInfo = getBasicInfo(companyNameSearch);
        returnList.add(basicInfo);
        String url = basicInfo.get(EXCEL_HEADER.get(11));
        List<Map<String, String>> riskList = getCompanyRisk(url);
        returnList.addAll(riskList);
        return returnList;
    }

    @Override
    public void getCompanyInfoBatch(List<String> nameList) throws IOException {
        List<Map<String, String>> writeList = new ArrayList<>();
        List<Callable<List<Map<String, String>>>> callableList = new ArrayList<>();
        for (String name : nameList) {
            Callable<List<Map<String, String>>> callable = () -> {
                List<Map<String, String>> returnErrorList = new ArrayList<>();
                try {
                    return getCompanyInfo(name);
                } catch (BizException e) {
                    Map<String, String> errorMap = new LinkedHashMap<>();
                    errorMap.put(EXCEL_HEADER.get(1), name);
                    errorMap.put(EXCEL_HEADER.get(2), e.getMessage());
                    returnErrorList.add(errorMap);
                } catch (Exception e) {
                    e.printStackTrace();
                    Map<String, String> errorMap = new LinkedHashMap<>();
                    errorMap.put(EXCEL_HEADER.get(1), name);
                    errorMap.put(EXCEL_HEADER.get(2), "系统错误");
                    returnErrorList.add(errorMap);
                } finally {
                    /*换行*/
                    returnErrorList.add(new LinkedHashMap<>());
                }
                return returnErrorList;
            };
            callableList.add(callable);
        }
        List<List<Map<String, String>>> resultList = blockedThreadPool.submitAllSynchronous(callableList);
        for (List<Map<String, String>> maps : resultList) {
            writeList.addAll(maps);
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ExcelWriter writer = ExcelUtil.getBigWriter();
        writer.write(writeList, true);
        writer.setColumnWidth(-1, 22);
        writer.flush(bout);
        writer.close();
        FileOutputStream fileOutputStream = new FileOutputStream("C:\\Users\\tc704\\Desktop\\a\\a.xlsx");
        fileOutputStream.write(bout.toByteArray());
        fileOutputStream.flush();
        fileOutputStream.close();
    }

    private List<Map<String, String>> getCompanyRisk(String url) throws IOException {
        List<Map<String, String>> returnList = new ArrayList<>();
        WebClient webClient = webClientInit();
        HtmlPage page = webClient.getPage(url + "?tab=risk");
        List<Object> dataList = page.getByXPath("//*[@id=\"detail-main\"]/div[1]/div[2]");
        if (dataList == null || dataList.size() == 0) {
            return returnList;
        }
        HtmlDivision dataDivision = (HtmlDivision) dataList.get(0);
        Iterable<DomElement> childElements = dataDivision.getChildElements();
        for (DomElement childElement : childElements) {
            HtmlDivision next = (HtmlDivision) childElement;
            /*有内容*/
            if (next.getChildElementCount() > 0) {
                List<Map<String, String>> riskTableData = getRiskTableData((DomElement) next.getFirstChild());
                returnList.addAll(riskTableData);
            }
        }
        webClient.close();
        return returnList;
    }

    private List<Map<String, String>> getRiskTableData(DomElement next) {
        List<Map<String, String>> returnList = new ArrayList<>();
        for (DomElement childElement : next.getChildElements()) {
            if (childElement instanceof HtmlHeading3) {
                HtmlHeading3 title = (HtmlHeading3) childElement;
                String titleString = title.getFirstChild().toString();
                Map<String, String> titleMap = new LinkedHashMap<>();
                titleMap.put(EXCEL_HEADER.get(1), "风险");
                titleMap.put(EXCEL_HEADER.get(2), titleString);
                returnList.add(titleMap);
            }
            if (childElement instanceof HtmlTable) {
                HtmlTable tableElement = (HtmlTable) childElement;
                Iterable<DomElement> childElements = tableElement.getChildElements();
                for (DomElement element : childElements) {
                    if (element instanceof HtmlTableHeader) {
                        int index = 1;
                        HtmlTableHeader htmlTableHeader = (HtmlTableHeader) element;
                        Iterable<DomElement> childElements1 = htmlTableHeader.getChildElements();
                        for (DomElement domElement : childElements1) {
                            Map<String, String> dataMap = new LinkedHashMap<>();
                            Iterable<DomElement> childElements2 = domElement.getChildElements();
                            for (DomElement domElement1 : childElements2) {
                                dataMap.put(EXCEL_HEADER.get(index) == null ? CommonConstants.NOTHING : EXCEL_HEADER.get(index), domElement1.getVisibleText());
                                index++;
                            }
                            returnList.add(dataMap);
                        }
                    }
                    if (element instanceof HtmlTableBody) {
                        HtmlTableBody htmlTableBody = (HtmlTableBody) element;
                        Iterable<DomElement> childElements1 = htmlTableBody.getChildElements();
                        List<Map<String, String>> collectList = new ArrayList<>();
                        for (DomElement domElement : childElements1) {
                            Iterable<DomElement> childElements2 = domElement.getChildElements();
                            Map<String, String> dataMap = new LinkedHashMap<>();
                            int index = 1;
                            for (DomElement domElement1 : childElements2) {
                                dataMap.put(EXCEL_HEADER.get(index) == null ? CommonConstants.NOTHING : EXCEL_HEADER.get(index), domElement1.getVisibleText());
                                index++;
                            }
                            collectList.add(dataMap);
                        }
                        returnList.addAll(collectList);
                    }
                }
            }
        }
        return returnList;
    }

    private Map<String, String> getBasicInfo(String companyNameSearch) throws IOException, BizException, InterruptedException {
        Map<String, String> companyInfoMap = new LinkedHashMap<>();
        companyInfoMap.put(EXCEL_HEADER.get(1), companyNameSearch);
        /*爬虫定义*/
        WebClient webClient = webClientInit();
        /*向爱企查获取页面*/
        String encode = URLEncoder.encode(companyNameSearch, "utf-8");
        String url = "https://aiqicha.baidu.com/s?q=" + encode + "&t=0";
        HtmlPage page = clickSearch(webClient, url);
        /*公司名称*/
        String companyName = getCompanyName(page);
        companyInfoMap.put(EXCEL_HEADER.get(2), companyName);
        /*统一社会信用代码*/
        String socialCreditCode = pageTableText(page, "//*[@id=\"basic-business\"]/table/tbody/tr[4]/td[2]");
        companyInfoMap.put(EXCEL_HEADER.get(3), socialCreditCode);
        /*法定代表人*/
        String personInLaw = getPersonInLaw(page);
        companyInfoMap.put(EXCEL_HEADER.get(4), personInLaw);
        /*注册资本*/
        String signInMoney = pageTableText(page, "//*[@id=\"basic-business\"]/table/tbody/tr[2]/td[2]");
        companyInfoMap.put(EXCEL_HEADER.get(5), signInMoney);
        /*实缴资本*/
        String moneyInCount = pageTableText(page, "//*[@id=\"basic-business\"]/table/tbody/tr[2]/td[4]");
        companyInfoMap.put(EXCEL_HEADER.get(6), moneyInCount);
        /*所属行业*/
        String inBusiness = pageTableText(page, "//*[@id=\"basic-business\"]/table/tbody/tr[3]/td[4]");
        companyInfoMap.put(EXCEL_HEADER.get(7), inBusiness);
        /*注册地址*/
        String signInAddress = pageDoomText(page, "//*[@id=\"basic-business\"]/table/tbody/tr[9]/td[2]/text()");
        companyInfoMap.put(EXCEL_HEADER.get(8), signInAddress);
        /*经营范围*/
        String businessRange = pageDoomText(page, "//*[@id=\"basic-business\"]/table/tbody/tr[10]/td[2]/div/text()");
        companyInfoMap.put(EXCEL_HEADER.get(9), businessRange);
        /*联系方式*/
        String contractPhone = getContractPhone(page);
        companyInfoMap.put(EXCEL_HEADER.get(10), contractPhone);
        companyInfoMap.put(EXCEL_HEADER.get(11), page.getUrl().toString());
        Thread.sleep(5000);
        page.executeJavaScript("");
        page.executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
        webClient.close();
        return companyInfoMap;
    }

    private String getContractPhone(HtmlPage page) {
        List<Object> dataList = page.getByXPath("/html/body/div[1]/div[1]/div/div[1]/div[1]/div[2]/div[3]/div[1]/div/span[1]");
        if (dataList == null || dataList.size() == 0) {
            return CommonConstants.NOTHING;
        }
        System.out.println(dataList.get(0));
        HtmlSpan dataSpan = (HtmlSpan) dataList.get(0);
        return dataSpan.getFirstChild().toString();
    }

    private String pageDoomText(HtmlPage page, String xPath) {
        List<Object> dataList = page.getByXPath(xPath);
        if (dataList == null || dataList.size() == 0) {
            return CommonConstants.NOTHING;
        }
        DomText dataAnchor = (DomText) dataList.get(0);
        return dataAnchor.toString();
    }

    private String pageTableText(HtmlPage page, String xPath) {
        List<Object> dataList = page.getByXPath(xPath);
        if (dataList == null || dataList.size() == 0) {
            return CommonConstants.NOTHING;
        }
        HtmlTableDataCell dataAnchor = (HtmlTableDataCell) dataList.get(0);
        DomNode firstChild = dataAnchor.getFirstChild();
        return firstChild.toString();
    }

    private String getPersonInLaw(HtmlPage page) {
        List<Object> byXPath = page.getByXPath("//*[@id=\"basic-business\"]/table/tbody/tr[1]/td[2]/div[2]/a[1]");
        if (byXPath == null || byXPath.size() == 0) {
            return CommonConstants.NOTHING;
        }
        /*找到搜索位第一位并点击*/
        HtmlAnchor searchHref = (HtmlAnchor) byXPath.get(0);
        return searchHref.getVisibleText();
    }

    private String getCompanyName(HtmlPage page) {
        List<Object> companyNameList = page.getByXPath("//*[@id=\"aqc-header-search-input\"]");
        /*对XML进行解析*/
        //公司名称
        if (companyNameList == null || companyNameList.size() == 0) {
            return CommonConstants.NOTHING;
        }
        HtmlTextInput companyNameAnchor = (HtmlTextInput) companyNameList.get(0);
        String text = companyNameAnchor.getText();
        /*改写input输入框*/
        Random random = new Random();
        String textChange = random.nextInt(100000) + "";
        StringBuilder textTemp = new StringBuilder();
        for (int i = 0; i < textChange.length(); i++) {
            textTemp.append(textChange);
            try {
                Thread.sleep(400);
            } catch (InterruptedException ignore) {
            }
            companyNameAnchor.setText(textTemp.toString());
        }
        return text;
    }

    /**
     * 返回详细页
     *
     * @param webClient webClient
     * @param url       url
     * @return HtmlPage详细页
     * @throws IOException  IOException
     * @throws BizException BizException
     */
    private HtmlPage clickSearch(WebClient webClient, String url) throws IOException, BizException, InterruptedException {
        HtmlPage page = webClient.getPage(url);
        Thread.sleep(5000);
        System.out.println(page.asXml());
        List<Object> byXPath = page.getByXPath("/html/body/div[1]/div[1]/div/div[1]/div[2]/div[2]/div/div[1]/div[2]/div/h3/a");
        if (byXPath == null || byXPath.size() == 0) {
            throw BizException.LAW_EXCEPTION.newInstance("未搜索到相关公司");
        }
        page.executeJavaScript("window.scrollTo(0, document.body.scrollHeight)");
        Thread.sleep(5000);
        /*找到搜索位第一位并点击*/
        HtmlAnchor searchHref = (HtmlAnchor) byXPath.get(0);
        url = "https://aiqicha.baidu.com" + searchHref.getHrefAttribute();
        page = webClient.getPage(url);
        return page;
    }

    /**
     * @return webClient生成
     */
    private WebClient webClientInit() {
        WebClient webClient = new WebClient(BrowserVersion.CHROME);
        webClient.getOptions().setThrowExceptionOnScriptError(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setUseInsecureSSL(true);
        webClient.getOptions().setCssEnabled(true);
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.getOptions().setDoNotTrackEnabled(true);
        webClient.getOptions().setGeolocationEnabled(true);
        webClient.getOptions().setActiveXNative(true);
        webClient.getOptions().setWebSocketEnabled(true);
        webClient.getOptions().setDownloadImages(true);
        webClient.getOptions().setAppletEnabled(true);
        webClient.getOptions().setPopupBlockerEnabled(true);
        webClient.getOptions().setRedirectEnabled(true);
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        webClient.setJavaScriptTimeout(100000000);
        webClient.getOptions().setTimeout(1000000);
        webClient.waitForBackgroundJavaScript(30000000);
        return webClient;
    }
}

