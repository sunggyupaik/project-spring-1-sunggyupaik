package com.example.bookclub.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookApiController.class)
class BookApiControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void detailBookBestSeller() throws Exception {
        mockMvc.perform(
                get("/api/book/bestseller")
        )
                .andDo(print())
                .andExpect(status().isOk());
    }
}