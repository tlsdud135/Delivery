package com.ldif.delivery.order.application.service;

import com.ldif.delivery.address.entity.Address;
import com.ldif.delivery.address.repository.AddressRepository;
import com.ldif.delivery.menu.domain.entity.MenuEntity;
import com.ldif.delivery.menu.domain.repository.MenuRepository;
import com.ldif.delivery.order.domain.entity.OrderEntity;
import com.ldif.delivery.order.domain.entity.OrderItemEntity;
import com.ldif.delivery.order.domain.entity.OrderStatus;
import com.ldif.delivery.order.domain.entity.OrderType;
import com.ldif.delivery.order.domain.repository.OrderRepository;
import com.ldif.delivery.order.exception.OrderBusinessException;
import com.ldif.delivery.order.exception.OrderNotFoundException;
import com.ldif.delivery.order.presentation.dto.OrderCreateRequest;
import com.ldif.delivery.order.presentation.dto.OrderResponse;
import com.ldif.delivery.order.presentation.dto.OrderStatusRequest;
import com.ldif.delivery.order.presentation.dto.OrderUpdateRequest;
import com.ldif.delivery.store.domain.entity.StoreEntity;
import com.ldif.delivery.store.domain.repository.StoreRepository;
import com.ldif.delivery.user.domain.entity.UserEntity;
import com.ldif.delivery.user.domain.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceV1Test {

    @Mock OrderRepository   orderRepository;
    @Mock MenuRepository    menuRepository;
    @Mock StoreRepository   storeRepository;
    @Mock UserRepository    userRepository;
    @Mock AddressRepository addressRepository;

    @InjectMocks OrderServiceV1 orderService;

    // 공통 픽스처
    private final String CUSTOMER_ID    = "customer1";
    private final String OWNER_ID       = "owner1";
    private final UUID STORE_ID         = UUID.randomUUID();
    private final UUID ADDRESS_ID       = UUID.randomUUID();
    private final UUID MENU_ID_1        = UUID.randomUUID();
    private final UUID MENU_ID_2        = UUID.randomUUID();
    private final UUID ORDER_ID         = UUID.randomUUID();

    private UserEntity  mockCustomer;
    private StoreEntity mockStore;
    private Address     mockAddress;
    private MenuEntity  mockMenu1;
    private MenuEntity  mockMenu2;
    private OrderEntity mockOrder;

    @BeforeEach
    void setUp() {
        // Mock Entity 설정

        mockCustomer = mock(UserEntity.class);
        given(mockCustomer.getUsername()).willReturn(CUSTOMER_ID);

        mockStore = mock(StoreEntity.class);
        given(mockStore.getStoreId()).willReturn(STORE_ID);
        given(mockStore.isHidden()).willReturn(false);
        given(mockStore.getOwner()).willReturn(mock(UserEntity.class));
        given(mockStore.getOwner().getUsername()).willReturn(OWNER_ID);

        mockAddress = mock(Address.class);
        given(mockAddress.getAddressId()).willReturn(ADDRESS_ID);

        mockMenu1 = mock(MenuEntity.class);
        given(mockMenu1.getMenuId()).willReturn(MENU_ID_1);
        given(mockMenu1.getStoreId()).willReturn(STORE_ID);
        given(mockMenu1.getPrice()).willReturn(10000);
        given(mockMenu1.getIsHidden()).willReturn(false);

        mockMenu2 = mock(MenuEntity.class);
        given(mockMenu2.getMenuId()).willReturn(MENU_ID_2);
        given(mockMenu2.getStoreId()).willReturn(STORE_ID);
        given(mockMenu2.getPrice()).willReturn(5000);
        given(mockMenu2.getIsHidden()).willReturn(false);

        // mockOrder 생성
        mockOrder = OrderEntity.create(
                mockCustomer,
                mockStore,
                mockAddress,
                OrderType.ONLINE,
                "덜 맵게 조리 부탁드립니다.",
                List.of(
                        OrderItemEntity.of(mockMenu1, 2, 10000),
                        OrderItemEntity.of(mockMenu2, 1, 5000)
                ),
                25000
        );
    }

    // ───────────────────────────────────────────────────────────
    // 주문 생성
    // ───────────────────────────────────────────────────────────
    @Nested
    @DisplayName("주문 생성 (createOrder)")
    class CreateOrder {
        private OrderCreateRequest validRequest() {
            return new OrderCreateRequest(
                    STORE_ID,
                    ADDRESS_ID,
                    "ONLINE",
                    "덜 맵게 조리 부탁드립니다.",
                    List.of(
                            new OrderCreateRequest.OrderItemRequest(MENU_ID_1, 2),
                            new OrderCreateRequest.OrderItemRequest(MENU_ID_2, 1)
                    )
            );
        }

        @Test
        @DisplayName("정상 주문 생성 성공")
        void success() {
            given(userRepository.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(mockCustomer));
            given(storeRepository.findById(STORE_ID))
                    .willReturn(Optional.of(mockStore));
            given(addressRepository.findById(ADDRESS_ID))
                    .willReturn(Optional.of(mockAddress));
            given(menuRepository.findAllById(any()))
                    .willReturn(List.of(mockMenu1, mockMenu2));
            given(orderRepository.save(any()))
                    .willAnswer(inv -> inv.getArgument(0));

            OrderResponse response = orderService.createOrder(validRequest(), CUSTOMER_ID);

            assertThat(response.customerId()).isEqualTo(CUSTOMER_ID);
            assertThat(response.totalPrice()).isEqualTo(25000);
            assertThat(response.status()).isEqualTo("PENDING");
            then(orderRepository).should(times(1)).save(any());
        }

        @Test
        @DisplayName("단가 스냅샷이 메뉴의 현재 단가로 저장")
        void unitPriceSnapshot() {
            given(userRepository.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(mockCustomer));
            given(storeRepository.findById(STORE_ID))
                    .willReturn(Optional.of(mockStore));
            given(addressRepository.findById(ADDRESS_ID))
                    .willReturn(Optional.of(mockAddress));
            given(menuRepository.findAllById(any()))
                    .willReturn(List.of(mockMenu1, mockMenu2));
            given(orderRepository.save(any()))
                    .willAnswer(inv -> inv.getArgument(0));

            OrderResponse response = orderService.createOrder(validRequest(), CUSTOMER_ID);

            assertThat(response.items())
                    .extracting("unitPrice")
                    .containsExactlyInAnyOrder(10000, 5000);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 주문 시 예외 발생")
        void failWhenCustomerNotFound() {
            given(userRepository.findById(CUSTOMER_ID))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(validRequest(), CUSTOMER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 사용자");
        }

        @Test
        @DisplayName("존재하지 않는 가게에 주문 시 예외 발생")
        void failWhenStoreNotFound() {
            given(userRepository.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(mockCustomer));
            given(storeRepository.findById(STORE_ID))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(validRequest(), CUSTOMER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 가게");
        }

        @Test
        @DisplayName("숨김 처리된 가게에 주문 시 예외 발생")
        void failWhenStoreIsHidden() {
            given(userRepository.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(mockCustomer));
            given(storeRepository.findById(STORE_ID))
                    .willReturn(Optional.of(mockStore));
            given(mockStore.isHidden()).willReturn(true);

            assertThatThrownBy(() -> orderService.createOrder(validRequest(), CUSTOMER_ID))
                    .isInstanceOf(OrderBusinessException.class)
                    .hasMessageContaining("운영 중이지 않은");
        }

        @Test
        @DisplayName("존재하지 않는 메뉴 포함 시 예외 발생")
        void failWhenMenuNotFound() {
            given(userRepository.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(mockCustomer));
            given(storeRepository.findById(STORE_ID))
                    .willReturn(Optional.of(mockStore));
            given(addressRepository.findById(ADDRESS_ID))
                    .willReturn(Optional.of(mockAddress));
            given(menuRepository.findAllById(any()))
                    .willReturn(List.of(mockMenu1));    // menu2 누락

            assertThatThrownBy(() -> orderService.createOrder(validRequest(), CUSTOMER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("존재하지 않는 메뉴");
        }

        @Test
        @DisplayName("다른 가게 메뉴 포함 시 예외 발생")
        void failWhenMenuNotBelongToStore() {
            given(mockMenu2.getStoreId())
                    .willReturn(UUID.randomUUID()); // 다른 가게
            given(userRepository.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(mockCustomer));
            given(storeRepository.findById(STORE_ID))
                    .willReturn(Optional.of(mockStore));
            given(addressRepository.findById(ADDRESS_ID))
                    .willReturn(Optional.of(mockAddress));
            given(menuRepository.findAllById(any()))
                    .willReturn(List.of(mockMenu1, mockMenu2));

            assertThatThrownBy(() -> orderService.createOrder(validRequest(), CUSTOMER_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("해당 가게의 메뉴가 아닙니다");
        }

        @Test
        @DisplayName("숨김 처리된 메뉴 포함 시 예외 발생")
        void failWhenMenuIsHidden() {
            given(mockMenu2.getIsHidden())
                    .willReturn(true);
            given(userRepository.findById(CUSTOMER_ID))
                    .willReturn(Optional.of(mockCustomer));
            given(storeRepository.findById(STORE_ID))
                    .willReturn(Optional.of(mockStore));
            given(addressRepository.findById(ADDRESS_ID))
                    .willReturn(Optional.of(mockAddress));
            given(menuRepository.findAllById(any()))
                    .willReturn(List.of(mockMenu1, mockMenu2));

            assertThatThrownBy(() -> orderService.createOrder(validRequest(), CUSTOMER_ID))
                    .isInstanceOf(OrderBusinessException.class)
                    .hasMessageContaining("주문할 수 없는 메뉴");
        }
    }

    // ───────────────────────────────────────────────────────────
    // 주문 상세 조회
    // ───────────────────────────────────────────────────────────
    @Nested
    @DisplayName("주문 상세 조회 (getOrder)")
    class GetOrder {

        @Test
        @DisplayName("CUSTOMER 본인 주문 조회 성공")
        void customerCanReadOwnOrder() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            assertThatCode(() ->
                    orderService.getOrder(ORDER_ID, CUSTOMER_ID, "CUSTOMER"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("CUSTOMER 타인 주문 조회 시 예외 발생")
        void customerCannotReadOtherOrder() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            assertThatThrownBy(() ->
                    orderService.getOrder(ORDER_ID, "other_customer", "CUSTOMER"))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("MANAGER는 모든 주문 조회 가능")
        void managerCanReadAnyOrder() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            assertThatCode(() ->
                    orderService.getOrder(ORDER_ID, "manager1", "MANAGER"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("존재하지 않는 주문 조회 시 예외 발생")
        void failWhenOrderNotFound() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() ->
                    orderService.getOrder(ORDER_ID, CUSTOMER_ID, "CUSTOMER"))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    // ───────────────────────────────────────────────────────────
    // 주문 수정 – 요청사항
    // ───────────────────────────────────────────────────────────
    @Nested
    @DisplayName("주문 수정 (updateOrder)")
    class UpdateOrder {

        @Test
        @DisplayName("CUSTOMER 본인 PENDING 주문 요청사항 수정 성공")
        void customerUpdateRequestSuccess() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            OrderResponse response = orderService.updateOrder(
                    ORDER_ID,
                    new OrderUpdateRequest("많이 맵게 조리 부탁드립니다."),
                    CUSTOMER_ID,
                    "CUSTOMER");

            assertThat(response.request()).isEqualTo("많이 맵게 조리 부탁드립니다.");
        }

        @Test
        @DisplayName("CUSTOMER 타인 주문 요청사항 수정 시 예외 발생")
        void customerCannotUpdateOtherOrder() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            assertThatThrownBy(() -> orderService.updateOrder(
                    ORDER_ID,
                    new OrderUpdateRequest("단무지는 빼주세요."),
                    "other_customer",
                    "CUSTOMER"))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("MASTER는 PENDING 아닌 상태에서도 요청사항 수정 가능")
        void masterCanUpdateRequestAnyStatus() {
            mockOrder.changeStatus(OrderStatus.ACCEPTED);
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            assertThatCode(() -> orderService.updateOrder(
                    ORDER_ID,
                    new OrderUpdateRequest("마스터 요청사항 수정입니다."),
                    "master1",
                    "MASTER"))
                    .doesNotThrowAnyException();
        }
    }

    // ───────────────────────────────────────────────────────────
    // 주문 상태 변경 (OWNER/MANAGER/MASTER)
    // ───────────────────────────────────────────────────────────
    @Nested
    @DisplayName("주문 상태 변경 (changeStatus)")
    class ChangeStatus {

        @Test
        @DisplayName("OWNER 본인 가게 주문 상태 변경 성공")
        void ownerChangeStatusSuccess() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            OrderResponse response = orderService.changeStatus(
                    ORDER_ID,
                    new OrderStatusRequest(OrderStatus.ACCEPTED),
                    OWNER_ID,
                    "OWNER");

            assertThat(response.status()).isEqualTo("ACCEPTED");
        }

        @Test
        @DisplayName("OWNER 타인 가게 주문 상태 변경 시 예외 발생")
        void ownerCannotChangeOtherStoreOrder() {
            UserEntity otherOwner = mock(UserEntity.class);
            given(otherOwner.getUsername()).willReturn("other_owner");
            given(mockStore.getOwner()).willReturn(otherOwner);
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            assertThatThrownBy(() -> orderService.changeStatus(
                    ORDER_ID,
                    new OrderStatusRequest(OrderStatus.ACCEPTED),
                    OWNER_ID,
                    "OWNER"))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("잘못된 상태 변화 시 예외 발생")
        void failWhenInvalidTransition() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            // PENDING -> DELIVERING 건너 뜀
            assertThatThrownBy(() -> orderService.changeStatus(
                    ORDER_ID,
                    new OrderStatusRequest(OrderStatus.DELIVERING),
                    OWNER_ID,
                    "MANAGER"))
                    .isInstanceOf(OrderBusinessException.class);
        }
    }

    // ───────────────────────────────────────────────────────────
    // 주문 취소
    // ───────────────────────────────────────────────────────────
    @Nested
    @DisplayName("주문 취소 (cancelOrder)")
    class CancelOrder {

        @Test
        @DisplayName("CUSTOMER 5분 이내 취소 성공")
        void customerCancelWithinLimit() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            OrderResponse response = orderService.cancelOrder(
                    ORDER_ID, CUSTOMER_ID, "CUSTOMER");

            assertThat(response.status()).isEqualTo("CANCELLED");
        }

        @Test
        @DisplayName("CUSTOMER 타인 주문 취소 시 예외 발생")
        void customerCannotCancelOtherOrder() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(
                    ORDER_ID, "other_customer", "CUSTOMER"))
                    .isInstanceOf(SecurityException.class);
        }

        @Test
        @DisplayName("CUSTOMER 5분 초과 시 예외 발생")
        void failWhenOverCancelLimit() {
            // ReflectionTestUtils로 createdAt 강제 주입
            org.springframework.test.util.ReflectionTestUtils.setField(
                    mockOrder,
                    "createdAt",
                    java.time.LocalDateTime.now().plusMinutes(6)   // 6분 후
            );

            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            assertThatThrownBy(() -> orderService.cancelOrder(
                    ORDER_ID,
                    CUSTOMER_ID,
                    "CUSTOMER"))
                    .isInstanceOf(OrderBusinessException.class)
                    .hasMessageContaining("5분이 경과");
        }

        @Test
        @DisplayName("MASTER는 시간 제한 없이 취소 가능")
        void masterCanCancelAnytime() {
            // ReflectionTestUtils로 createdAt 강제 주입
            org.springframework.test.util.ReflectionTestUtils.setField(
                    mockOrder,
                    "createdAt",
                    java.time.LocalDateTime.now().minusMinutes(10)   // 10분 후
            );

            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            assertThatCode(() -> orderService.cancelOrder(
                    ORDER_ID, "master1", "MASTER"))
                    .doesNotThrowAnyException();
        }
    }

    // ───────────────────────────────────────────────────────────
    // 주문 삭제 (소프트)
    // ───────────────────────────────────────────────────────────
    @Nested
    @DisplayName("주문 소프트 삭제 (deleteOrder)")
    class DeleteOrder {

        @Test
        @DisplayName("MASTER 소프트 삭제 성공")
        void masterSoftDeleteSuccess() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.of(mockOrder));

            orderService.deleteOrder(ORDER_ID, "master1");

            assertThat(mockOrder.getDeletedAt()).isNotNull();
            assertThat(mockOrder.getDeletedBy()).isEqualTo("master1");
        }

        @Test
        @DisplayName("존재하지 않는 주문 삭제 시 예외 발생")
        void failWhenOrderNotFound() {
            given(orderRepository.findActiveById(ORDER_ID))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.deleteOrder(
                    ORDER_ID, "master1"))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }
}