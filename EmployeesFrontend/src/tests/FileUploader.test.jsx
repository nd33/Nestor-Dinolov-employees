import React from 'react'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import FileUploader from '../FileUploader'
import { vi } from 'vitest'

describe('FileUploader Component', () => {
  const mockOnFileSelect = vi.fn()
  const mockOnAnalyze = vi.fn()
  
  beforeEach(() => {
    vi.clearAllMocks()
  })
  
  test('renders file input and analyze button', () => {
    render(
      <FileUploader
        onFileSelect={mockOnFileSelect}
        onAnalyze={mockOnAnalyze}
        selectedFile={null}
        loading={false}
      />
    )
    
    expect(screen.getByLabelText(/select csv file/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /analyze employee pairs/i })).toBeInTheDocument()
  })
  
  test('analyze button is disabled when no file selected', () => {
    render(
      <FileUploader
        onFileSelect={mockOnFileSelect}
        onAnalyze={mockOnAnalyze}
        selectedFile={null}
        loading={false}
      />
    )
    
    expect(screen.getByRole('button')).toBeDisabled()
  })
  
  test('analyze button is enabled when file selected', () => {
    const mockFile = new File(['test'], 'test.csv', { type: 'text/csv' })
    
    render(
      <FileUploader
        onFileSelect={mockOnFileSelect}
        onAnalyze={mockOnAnalyze}
        selectedFile={mockFile}
        loading={false}
      />
    )
    
    expect(screen.getByRole('button')).not.toBeDisabled()
  })
  
  test('analyze button shows loading state', () => {
    render(
      <FileUploader
        onFileSelect={mockOnFileSelect}
        onAnalyze={mockOnAnalyze}
        selectedFile={new File(['test'], 'test.csv')}
        loading={true}
      />
    )
    
    expect(screen.getByText(/analyzing/i)).toBeInTheDocument()
    expect(screen.getByRole('button')).toBeDisabled()
  })
  
  test('calls onFileSelect when file is selected', async () => {
    render(
      <FileUploader
        onFileSelect={mockOnFileSelect}
        onAnalyze={mockOnAnalyze}
        selectedFile={null}
        loading={false}
      />
    )
    
    const file = new File(['test'], 'test.csv', { type: 'text/csv' })
    const input = screen.getByLabelText(/select csv file/i)
    
    await userEvent.upload(input, file)
    
    expect(mockOnFileSelect).toHaveBeenCalled()
  })
  
  test('calls onAnalyze when button is clicked', () => {
    const mockFile = new File(['test'], 'test.csv', { type: 'text/csv' })
    
    render(
      <FileUploader
        onFileSelect={mockOnFileSelect}
        onAnalyze={mockOnAnalyze}
        selectedFile={mockFile}
        loading={false}
      />
    )
    
    fireEvent.click(screen.getByRole('button'))
    
    expect(mockOnAnalyze).toHaveBeenCalled()
  })
})