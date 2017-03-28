package lab3_2;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.application.api.command.AddProductCommand;
import pl.com.bottega.ecommerce.sales.application.api.handler.AddProductCommandHandler;
import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.client.ClientRepository;
import pl.com.bottega.ecommerce.sales.domain.equivalent.SuggestionService;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductRepository;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sales.domain.reservation.ReservationRepository;
import pl.com.bottega.ecommerce.sales.domain.reservation.Reservation;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import pl.com.bottega.ecommerce.sharedkernel.exceptions.DomainOperationException.DomainOperationException;

public class TAddProductCommandHandler {
	AddProductCommandHandler handler;
	ReservationRepository rR;
	ClientRepository cR;
	ProductRepository pR;
	SuggestionService sS;
	AddProductCommand command;

	@Before
	public void setup() {
		handler = new AddProductCommandHandler();
		rR = Mockito.mock(ReservationRepository.class);
		cR = Mockito.mock(ClientRepository.class);
		pR = Mockito.mock(ProductRepository.class);
		sS = Mockito.mock(SuggestionService.class);

		command = new AddProductCommand(Id.generate(), Id.generate(), 123);

		Whitebox.setInternalState(handler, "reservationRepository", rR);
		Whitebox.setInternalState(handler, "clientRepository", cR);
		Whitebox.setInternalState(handler, "productRepository", pR);
		Whitebox.setInternalState(handler, "suggestionService", sS);
	}

	@Test
	public void availableItemDoesntSuggestAnything() {
		Product p1 = new Product(Id.generate(), new Money(5), "test", ProductType.FOOD);
		Product p2 = new Product(Id.generate(), new Money(8), "test4", ProductType.FOOD);
		Reservation res = new Reservation(Id.generate(), Reservation.ReservationStatus.OPENED,
				new ClientData(Id.generate(), "wkurwiony tester"), new Date());

		Client c = new Client();

		Mockito.when(pR.load(command.getProductId())).thenReturn(p1);
		Mockito.when(rR.load(command.getOrderId())).thenReturn(res);
		Mockito.when(cR.load(Mockito.any(Id.class))).thenReturn(c);
		Mockito.when(sS.suggestEquivalent(Mockito.any(Product.class), Mockito.any(Client.class))).thenReturn(p2);

		handler.handle(command);

		Mockito.verify(sS, Mockito.times(0)).suggestEquivalent(p1, c);
	}

	@Test(expected = DomainOperationException.class)
	public void productAddedToClosedReservationThrowsException() {
		Product p1 = new Product(Id.generate(), new Money(5), "test", ProductType.FOOD);
		Product p2 = new Product(Id.generate(), new Money(8), "test4", ProductType.FOOD);
		Reservation res = new Reservation(Id.generate(), Reservation.ReservationStatus.CLOSED,
				new ClientData(Id.generate(), "troszke mniej zdenerwowany tester"), new Date());

		Client c = new Client();

		Mockito.when(pR.load(command.getProductId())).thenReturn(p1);
		Mockito.when(rR.load(command.getOrderId())).thenReturn(res);
		Mockito.when(cR.load(Mockito.any(Id.class))).thenReturn(c);
		Mockito.when(sS.suggestEquivalent(Mockito.any(Product.class), Mockito.any(Client.class))).thenReturn(p2);

		handler.handle(command);
	}
}
