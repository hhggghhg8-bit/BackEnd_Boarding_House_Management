package com.example.QLPT.Enity.Enum;

public class Enum {
    public enum Role { ADMIN, LANDLORD, TENANT }
    public enum UserStatus { ACTIVE, INACTIVE, BLOCKED }
    public enum RoomStatus { AVAILABLE, RENTED, MAINTENANCE }
    public enum ContractStatus { ACTIVE, EXPIRED, TERMINATED }
    public enum DepositStatus { UNPAID, PAID, REFUNDED, FORFEITED }
    public enum RoommateStatus { ACTIVE, MOVED_OUT }
    public enum InvoiceStatus { UNPAID, PARTIALLY_PAID, PAID, OVERDUE }
    public enum IncidentStatus { PENDING, PROCESSING, RESOLVED }
    public enum NotificationType { SYSTEM, INVOICE, INCIDENT, CONTRACT }
    public enum HistoryAction { CREATED, UPDATED, RENEWED, TERMINATED }
    public enum ReferenceType { PROPERTY, ROOM, ROOM_BASE_PRICE, ROOM_SERVICE_PRICE }
}
