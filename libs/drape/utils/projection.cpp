#include "drape/utils/projection.hpp"

namespace dp
{
std::array<float, 16> MakeProjection([[maybe_unused]] dp::ApiVersion apiVersion, float left, float right, float bottom, float top)
{
  std::array<float, 16> result = {};

  // Projection matrix is calculated for [-1;1] depth-space.
  float constexpr depthScale = 1.0f;
  float constexpr depthOffset = 0.0f;

  float const width = right - left;
  float const height = top - bottom;
  float const depth = kMaxDepth - kMinDepth;

  result[0] = 2.0f / width;
  result[3] = -(right + left) / width;
  result[5] = 2.0f / height;
  result[7] = -(top + bottom) / height;
  result[10] = -2.0f * depthScale / depth;
  result[11] = depthOffset - (kMaxDepth + kMinDepth) / depth;
  result[15] = 1.0;

  return result;
}
}  // namespace dp
