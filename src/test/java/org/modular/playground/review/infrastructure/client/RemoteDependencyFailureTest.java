package org.modular.playground.review.infrastructure.client;
import jakarta.ws.rs.ServiceUnavailableException;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RemoteDependencyFailureTest {
 @Test void catalogFailureBecomesUnavailable() {
  var client=mock(CatalogServiceClient.class); var service=new RemoteBookService(); service.catalogClient=client;
  var ids=List.of(UUID.randomUUID()); var cause=new IllegalStateException("offline");
  when(client.getBooksByIds(ids)).thenThrow(cause);
  var error=assertThrows(ServiceUnavailableException.class,()->service.getBooksByIds(ids));
  assertEquals(503,error.getResponse().getStatus()); assertSame(cause,error.getCause());
 }
 @Test void userFailureBecomesUnavailable() {
  var client=mock(UserServiceClient.class); var service=new RemoteUserService(); service.userClient=client;
  var ids=List.of(UUID.randomUUID()); var cause=new IllegalStateException("offline");
  when(client.getUsersByIds(ids)).thenThrow(cause);
  var error=assertThrows(ServiceUnavailableException.class,()->service.findUsersByIds(ids));
  assertEquals(503,error.getResponse().getStatus()); assertSame(cause,error.getCause());
 }
 @Test void successfulEmptyResultsRemainEmpty() {
  var books=mock(CatalogServiceClient.class); var users=mock(UserServiceClient.class);
  var bookService=new RemoteBookService(); bookService.catalogClient=books;
  var userService=new RemoteUserService(); userService.userClient=users;
  var ids=List.of(UUID.randomUUID());
  when(books.getBooksByIds(ids)).thenReturn(List.of()); when(users.getUsersByIds(ids)).thenReturn(List.of());
  assertTrue(bookService.getBooksByIds(ids).isEmpty()); assertTrue(userService.findUsersByIds(ids).isEmpty());
 }
}
