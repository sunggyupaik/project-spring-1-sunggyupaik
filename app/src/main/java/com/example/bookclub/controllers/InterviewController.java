package com.example.bookclub.controllers;

import com.example.bookclub.application.InterviewService;
import com.example.bookclub.domain.Account;
import com.example.bookclub.domain.Interview;
import com.example.bookclub.security.CurrentAccount;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/interviews")
public class InterviewController {
    private final InterviewService interviewService;
    @Value("${countList}")
    private int countList;

    @Value("${countPage}")
    private int countPage;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    public String interviewLists(@CurrentAccount Account account,
                                 @RequestParam String targetPage,
                                 Model model) {
        if(account != null) {
            checkTopMenu(account, model);
        }
        List<Interview> lists = interviewService.getInterviews();
//        model.addAttribute("interviews", lists);

        int allInterviewsCount = lists.size();
        int totalPage = allInterviewsCount / countList;
        if(allInterviewsCount % countList > 0) {
            totalPage += 1;
        }
        int nowPage = Integer.parseInt(targetPage);
        if(nowPage > totalPage) {
            nowPage = totalPage;
        }
        int firstPage = ((nowPage - 1) / countPage) * countPage + 1;
        int lastPage = firstPage + countPage - 1;
        if(lastPage > totalPage) {
            lastPage = totalPage;
        }
        List<Interview> interviews = interviewService
                .getNowPageInterviews((nowPage - 1) * countList, countList);
        
        //List<Interview> list = interviewService.crawlAllInterviews();
        return "interviews/interviews-list";
    }

    private void checkTopMenu(@CurrentAccount Account account, Model model) {
        model.addAttribute("account", account);
        if (account.isMangerOf(account.getStudy())) {
            model.addAttribute("studyManager", account.getStudy());
        }
        if (account.isApplierOf(account.getStudy())) {
            model.addAttribute("studyApply", account.getStudy());
        }
    }
}
