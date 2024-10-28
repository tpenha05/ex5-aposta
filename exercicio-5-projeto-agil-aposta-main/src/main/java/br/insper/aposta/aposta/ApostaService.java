package br.insper.aposta.aposta;

import br.insper.aposta.partida.PartidaNaoEncontradaException;
import br.insper.aposta.partida.PartidaNaoRealizadaException;
import br.insper.aposta.partida.PartidaService;
import br.insper.aposta.partida.RetornarPartidaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApostaService {

    @Autowired
    private ApostaRepository apostaRepository;

    @Autowired
    private PartidaService partidaService;

    public Aposta salvar(Aposta aposta) {
        aposta.setId(UUID.randomUUID().toString());

        ResponseEntity<RetornarPartidaDTO> partida = partidaService.getPartida(aposta.getIdPartida());

        if (partida.getStatusCode().is2xxSuccessful())  {
            aposta.setStatus("REALIZADA");
            aposta.setDataAposta(LocalDateTime.now());

            return apostaRepository.save(aposta);
        } else {
            throw new PartidaNaoEncontradaException("Partida não encontrada");
        }

    }

    public List<Aposta> listar() {
        return apostaRepository.findAll();
    }

    public Aposta getAposta(String idAposta) {

        Optional<Aposta> aposta = apostaRepository.findById(idAposta);

        if (aposta.isPresent()) {
            return aposta.get();
        } else {
            throw new ApostaNaoEncontradaException("Aposta não encontrada");
        }

    }

    @KafkaListener(topics = "partidas")
    public void atualizaApostas(RetornarPartidaDTO partida) {

        for (Aposta aposta : apostaRepository.findByPartidaId(partida.getId())) {

            if (!aposta.getStatus().equals("REALIZADA")) {

                if (partida.getStatusCode().is2xxSuccessful()) {
                    RetornarPartidaDTO partidaDTO = partida.getBody();

                    if (partidaDTO.getStatus().equals("REALIZADA")) {

                        if (aposta.getResultado().equals("EMPATE") && partidaDTO.isEmpate()) {
                            aposta.setStatus("GANHOU");
                        }

                        if (aposta.getResultado().equals("VITORIA_MANDANTE") && partidaDTO.isVitoriaMandante()) {
                            aposta.setStatus("GANHOU");
                        }

                        if (aposta.getResultado().equals("EMPATE") && partidaDTO.isVitoriaVisitante()) {
                            aposta.setStatus("GANHOU");
                        }

                        if (aposta.getStatus().equals("REALIZADA")) {
                            aposta.setStatus("PERDEU");
                        }
                        apostaRepository.save(aposta);
                    }
                }
            }
        }
    }

}
