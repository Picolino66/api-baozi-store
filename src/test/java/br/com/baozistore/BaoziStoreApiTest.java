package br.com.baozistore;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class BaoziStoreApiTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    private long criar(String endpoint, String json) throws Exception {
        MvcResult result = mvc.perform(post(endpoint).contentType(MediaType.APPLICATION_JSON).content(json))
            .andExpect(status().isCreated()).andReturn();
        JsonNode body = mapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }

    @Test
    void fluxoCompletoERegrasDeIntegridade() throws Exception {
        long clienteId = criar("/clientes", "{\"nome\":\"Isaías Gonçalves Ribeiro5145055\",\"clienteDesde\":\"2026-10-12\"}");
        long produtoId = criar("/produtos", "{\"nome\":\"Baozi de porco\",\"preco\":12.50,\"estoque\":true}");
        long pedidoId = criar("/pedidos", "{\"clienteId\":" + clienteId + ",\"produtoId\":" + produtoId + ",\"quantidade\":3}");

        mvc.perform(get("/clientes")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nome").value("Isaías Gonçalves Ribeiro5145055"));
        mvc.perform(get("/produtos/" + produtoId)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("Baozi de porco"));
        mvc.perform(get("/pedidos")).andExpect(status().isOk()).andExpect(jsonPath("$[0].quantidade").value(3));
        mvc.perform(get("/pedidos/" + pedidoId)).andExpect(status().isOk()).andExpect(jsonPath("$.clienteId").value(clienteId));

        mvc.perform(put("/clientes/" + clienteId).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Isaías Gonçalves Ribeiro5145055\",\"clienteDesde\":\"2026-10-12\"}"))
            .andExpect(status().isOk());
        mvc.perform(put("/produtos/" + produtoId).contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Baozi de porco\",\"preco\":13.00,\"estoque\":true}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.preco").value(13.0));
        mvc.perform(put("/pedidos/" + pedidoId).contentType(MediaType.APPLICATION_JSON)
            .content("{\"clienteId\":" + clienteId + ",\"produtoId\":" + produtoId + ",\"quantidade\":4}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(4));

        mvc.perform(delete("/clientes/" + clienteId)).andExpect(status().isConflict());
        mvc.perform(delete("/produtos/" + produtoId)).andExpect(status().isConflict());
        mvc.perform(delete("/pedidos/" + pedidoId)).andExpect(status().isNoContent());
        mvc.perform(delete("/clientes/" + clienteId)).andExpect(status().isNoContent());
        mvc.perform(delete("/produtos/" + produtoId)).andExpect(status().isNoContent());
        mvc.perform(get("/pedidos/" + pedidoId)).andExpect(status().isNotFound());
    }

    @Test
    void rejeitaDadosInvalidosEReferenciasInexistentes() throws Exception {
        mvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"\",\"clienteDesde\":\"2026-10-12\"}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/produtos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Baozi\",\"preco\":0,\"estoque\":true}"))
            .andExpect(status().isBadRequest());
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"clienteId\":999999,\"produtoId\":999999,\"quantidade\":1}"))
            .andExpect(status().isNotFound());
        mvc.perform(post("/pedidos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"clienteId\":1,\"produtoId\":1,\"quantidade\":0}"))
            .andExpect(status().isBadRequest());
    }
}
