package com.awsdemo.productservice.api;

import com.awsdemo.productservice.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(ProductService.class)
class ProductControllerTest {

    private static final String VALID_BODY = """
            {"name":"Desk Lamp","price":1299.00,"category":"home"}
            """;

    @Autowired
    private MockMvc mvc;

    @Test
    void listReturnsStaticCatalogue() throws Exception {
        mvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(header().exists("X-Trace-Id"));
    }

    @Test
    void getExisting() throws Exception {
        mvc.perform(get("/api/v1/products/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Mechanical Keyboard"));
    }

    @Test
    void getMissingReturnsProblemDetailWithPropagatedTraceId() throws Exception {
        mvc.perform(get("/api/v1/products/99").header("X-Trace-Id", "test-trace-0001"))
                .andExpect(status().isNotFound())
                .andExpect(header().string("X-Trace-Id", "test-trace-0001"))
                .andExpect(jsonPath("$.title").value("Product Not Found"))
                .andExpect(jsonPath("$.traceId").value("test-trace-0001"));
    }

    @Test
    void createEchoesWithLocation() throws Exception {
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name").value("Desk Lamp"));
    }

    @Test
    void createRejectsInvalidBody() throws Exception {
        mvc.perform(post("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"price\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists())
                .andExpect(jsonPath("$.errors.category").exists());
    }

    @Test
    void updateEchoesWithPathId() throws Exception {
        mvc.perform(put("/api/v1/products/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    void deleteReturnsNoContent() throws Exception {
        mvc.perform(delete("/api/v1/products/3"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteMissingReturns404() throws Exception {
        mvc.perform(delete("/api/v1/products/42"))
                .andExpect(status().isNotFound());
    }
}
