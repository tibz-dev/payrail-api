# PayRail

Developer-first payment orchestration platform for South African merchants.

## Overview

PayRail is an API-first payment orchestration platform designed to provide
merchants and developers with a unified payment integration, checkout
experience, transaction lifecycle and webhook system.

The MVP currently uses simulated payment processing and does not move real
money.

## MVP

The MVP allows a merchant to:

- Register an account
- Generate API credentials
- Create payment requests
- Generate hosted checkout sessions
- Simulate payment outcomes
- Track payment status
- Receive webhooks
- View transactions
- View basic payment ledger information

## Architecture

```text
Merchant
   |
   v
PayRail API
   |
   +-- Merchant Service
   +-- Payment Service
   +-- Checkout
   +-- Webhook Service
   +-- Ledger
   |
   v
Payment Provider
   |
   v
Mock Payment Provider
