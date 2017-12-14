package de.lmu.settleBattle.catanServer;

import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestWebSocketSession implements WebSocketSession {

    private String id;

    private URI uri;

    private Map<String, Object> attributes = new HashMap<>();

    private Principal principal;

    private InetSocketAddress localAddress;

    private InetSocketAddress remoteAddress;

    private String protocol;

    private List<WebSocketExtension> extensions = new ArrayList<>();

    private boolean open;

    private final List<WebSocketMessage<?>> messages = new ArrayList<>();

    private CloseStatus status;

    private HttpHeaders headers;

    private static int idCounter =0;

    public TestWebSocketSession() {
        this.id = String.valueOf(idCounter);
        idCounter++;
    }

    public TestWebSocketSession(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public URI getUri() {
        return this.uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }


    @Override
    public HttpHeaders getHandshakeHeaders() {
        return this.headers;
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Principal getPrincipal() {
        return this.principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.remoteAddress;
    }

    public void setRemoteAddress(InetSocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public String getAcceptedProtocol() {
        return this.protocol;
    }

    public void setAcceptedProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public void setTextMessageSizeLimit(int messageSizeLimit) {
    }

    @Override
    public int getTextMessageSizeLimit() {
        return 0;
    }

    @Override
    public void setBinaryMessageSizeLimit(int messageSizeLimit) {
    }

    @Override
    public int getBinaryMessageSizeLimit() {
        return 0;
    }

    @Override
    public List<WebSocketExtension> getExtensions() {
        return this.extensions;
    }

    public void setExtensions(List<WebSocketExtension> extensions) {
        this.extensions = extensions;
    }

    @Override
    public boolean isOpen() {
        return this.open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public List<WebSocketMessage<?>> getSentMessages() {
        return this.messages;
    }

    public CloseStatus getCloseStatus() {
        return this.status;
    }

    @Override
    public void sendMessage(WebSocketMessage<?> message) throws IOException {
        this.messages.add(message);
    }

    @Override
    public void close() throws IOException {
        this.open = false;
    }

    @Override
    public void close(CloseStatus status) throws IOException {
        this.open = false;
        this.status = status;
    }

    public TextMessage get(int index) {
        return (TextMessage) messages.get(index);
    }

    public TextMessage getLast() {
        return (TextMessage) messages.get(messages.size()-1);
    }

    public int getLastIndex() {
        return messages.size()-1;
    }
}
