@bdd
Feature: 產品管理系統
  作為一個產品經理
  我希望能夠管理產品目錄
  以便追蹤和維護產品信息

  Background: 
    Given 系統中已存在以下產品
      | id | name             | price | category     |
      | 1  | Wireless Mouse   | 29.99 | Electronics  |
      | 2  | Mechanical Keyboard | 89.99 | Electronics |

  Scenario: 成功創建新產品
    When 我創建一個新產品
      | name             | price | category     |
      | Smart Watch      | 199.99| Electronics  |
    Then 系統應該返回創建的產品詳情
    And 產品列表中應包含新創建的產品

  Scenario: 更新現有產品
    When 我更新 ID 為 1 的產品
      | name             | price | category     |
      | Ergonomic Mouse  | 39.99 | Electronics  |
    Then 系統應該返回更新後的產品詳情
    And 產品名稱應該被成功更新

  Scenario: 刪除產品
    When 我刪除 ID 為 2 的產品
    Then 系統應該成功刪除該產品
    And 產品列表中不應包含已刪除的產品

  Scenario Outline: 驗證產品創建的輸入驗證
    When 我嘗試創建一個包含無效信息的產品
      | name   | price   | category   |
      | <name> | <price> | <category> |
    Then 系統應該拒絕創建並返回驗證錯誤

    Examples:
      | name              | price  | category     |
      |                   | 199.99 | Electronics  |
      | Smart Watch       | -10    | Electronics  |
      | Valid Product     | 100    |              |