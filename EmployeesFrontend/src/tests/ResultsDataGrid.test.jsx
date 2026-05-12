import React from 'react'
import { render, screen } from '@testing-library/react'
import ResultsDataGrid from '../ResultsDataGrid'

describe('ResultsDataGrid Component', () => {
  const mockResult = {
    empId1: 143,
    empId2: 217,
    totalDaysWorked: 747,
    commonProjects: [
      { projectId: 15, daysWorked: 717 },
      { projectId: 12, daysWorked: 30 }
    ]
  }
  
  test('renders nothing when result is null', () => {
    const { container } = render(<ResultsDataGrid result={null} />)
    expect(container.firstChild).toBeNull()
  })
  
  test('displays best pair information', () => {
    render(<ResultsDataGrid result={mockResult} />)
    
    expect(screen.getByText(/employee #143 and employee #217/i)).toBeInTheDocument()
    expect(screen.getByText(/747 days/i)).toBeInTheDocument()
  })
  
  test('displays project details table', () => {
    render(<ResultsDataGrid result={mockResult} />)
    
    expect(screen.getByText('Project ID')).toBeInTheDocument()
    expect(screen.getByText('Days Worked Together')).toBeInTheDocument()
    expect(screen.getByText('15')).toBeInTheDocument()
    expect(screen.getByText('12')).toBeInTheDocument()
    expect(screen.getByText('717 days')).toBeInTheDocument()
    expect(screen.getByText('30 days')).toBeInTheDocument()
  })
  
  test('displays correct number of rows', () => {
    render(<ResultsDataGrid result={mockResult} />)
    
    const rows = screen.getAllByRole('row')
    // Header row + 2 data rows = 3 rows
    expect(rows).toHaveLength(3)
  })
  
  test('handles single project case', () => {
    const singleProjectResult = {
      ...mockResult,
      commonProjects: [{ projectId: 15, daysWorked: 747 }]
    }
    
    render(<ResultsDataGrid result={singleProjectResult} />)
    
    const rows = screen.getAllByRole('row')
    expect(rows).toHaveLength(2) // Header + 1 data row
  })
  
  test('displays total days note', () => {
    render(<ResultsDataGrid result={mockResult} />)
    
    expect(screen.getByText(/total days \(747\)/i)).toBeInTheDocument()
  })
})