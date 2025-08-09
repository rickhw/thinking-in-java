import '@testing-library/jest-dom'

// Mock fetch globally for tests
global.fetch = vi.fn()

// Reset fetch mock before each test
beforeEach(() => {
  fetch.mockClear()
})