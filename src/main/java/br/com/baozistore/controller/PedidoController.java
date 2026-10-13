package br.com.baozistore.controller;

import java.net.URI;
import java.util.List;

import br.com.baozistore.model.Cliente;
import br.com.baozistore.model.Pedido;
import br.com.baozistore.model.Produto;
import br.com.baozistore.repository.ClienteRepository;
import br.com.baozistore.repository.PedidoRepository;
import br.com.baozistore.repository.ProdutoRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private final PedidoRepository pedidos;
    private final ClienteRepository clientes;
    private final ProdutoRepository produtos;

    public PedidoController(PedidoRepository pedidos, ClienteRepository clientes, ProdutoRepository produtos) {
        this.pedidos = pedidos;
        this.clientes = clientes;
        this.produtos = produtos;
    }

    public record PedidoDados(@NotNull Long clienteId, @NotNull Long produtoId, @NotNull @Min(1) Integer quantidade) { }
    public record PedidoResposta(Long id, Long clienteId, Long produtoId, Integer quantidade) {
        static PedidoResposta de(Pedido pedido) {
            return new PedidoResposta(pedido.getId(), pedido.getCliente().getId(), pedido.getProduto().getId(), pedido.getQuantidade());
        }
    }

    private Pedido preencher(Pedido pedido, PedidoDados dados) {
        Cliente cliente = clientes.findById(dados.clienteId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente não encontrado"));
        Produto produto = produtos.findById(dados.produtoId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto não encontrado"));
        pedido.setCliente(cliente);
        pedido.setProduto(produto);
        pedido.setQuantidade(dados.quantidade());
        return pedido;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<PedidoResposta> criar(@Valid @RequestBody PedidoDados dados) {
        Pedido salvo = pedidos.save(preencher(new Pedido(), dados));
        return ResponseEntity.created(URI.create("/pedidos/" + salvo.getId())).body(PedidoResposta.de(salvo));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<PedidoResposta> listar() { return pedidos.findAll().stream().map(PedidoResposta::de).toList(); }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public PedidoResposta consultar(@PathVariable Long id) { return PedidoResposta.de(buscar(id)); }

    private Pedido buscar(Long id) {
        return pedidos.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido não encontrado"));
    }

    @PutMapping("/{id}")
    @Transactional
    public PedidoResposta atualizar(@PathVariable Long id, @Valid @RequestBody PedidoDados dados) {
        return PedidoResposta.de(pedidos.save(preencher(buscar(id), dados)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable Long id) {
        pedidos.delete(buscar(id));
        return ResponseEntity.noContent().build();
    }
}
